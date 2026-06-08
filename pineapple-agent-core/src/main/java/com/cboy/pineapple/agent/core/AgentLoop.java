package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.agent.core.types.*;
import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.AbortSignal;
import com.cboy.pineapple.ai.Stream;
import com.cboy.pineapple.ai.types.Context;
import com.cboy.pineapple.ai.types.StreamOptions;
import com.cboy.pineapple.ai.types.ThinkingLevel;
import com.cboy.pineapple.ai.types.content.ContentTypeEnums;
import com.cboy.pineapple.ai.types.content.TextContent;
import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.message.*;
import com.cboy.pineapple.ai.utils.AssistantMessageEvent;
import com.cboy.pineapple.ai.utils.AssistantMessageEventStream;
import com.cboy.pineapple.ai.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class AgentLoop {


    public void runLoop(AgentContext initialContext, List<AgentMessage> newMessages, AgentLoopConfig initialConfig,
                          AbortSignal signal, AgentEventSink emit, StreamFn streamFn) throws ExecutionException, InterruptedException {
        AgentContext currentContext = initialContext;
        AgentLoopConfig config = initialConfig;
        boolean firstTurn = true;
        // Check for steering messages at start (user may have typed while waiting)
        List<AgentMessage> pendingMessages = config.getSteeringMessage();

        // Outer loop: continues when queued follow-up messages arrive after agent would stop
        while (true) {
            boolean hasMoreToolCalls = true;

            // Inner loop: process tool calls and steering messages
            while (hasMoreToolCalls || !pendingMessages.isEmpty()) {
                if (!firstTurn) {
                    emit.apply(new AgentEvent.TurnStart());
                } else {
                    firstTurn = false;
                }

                // Process pending messages (inject before next assistant response)
                if (!pendingMessages.isEmpty()) {
                    for (AgentMessage pendingMessage : pendingMessages) {
                        emit.apply(new AgentEvent.MessageStart(pendingMessage));
                        emit.apply(new AgentEvent.MessageEnd(pendingMessage));
                        currentContext.getMessages().add(pendingMessage);
                        newMessages.add(pendingMessage);
                    }
                    pendingMessages = new LinkedList<>();
                }

                // Stream assistant response
                CompletableFuture<AssistantMessage> message = streamAssistantResponse(currentContext, config, signal, emit, streamFn);
                AssistantMessage assistantMessage = message.get();
                newMessages.add(assistantMessage);

                if (StopReason.ERROR.equals(assistantMessage.stopReason()) || StopReason.ABORTED.equals(assistantMessage.stopReason())) {
                    emit.apply(new AgentEvent.TurnEnd(assistantMessage, new ArrayList<>()));
                    emit.apply(new AgentEvent.AgentEnd(newMessages));
                    return;
                }

                // Check for tool calls
                List<ToolCall> toolCalls = assistantMessage.content()
                        .stream()
                        .filter(c -> c.type().equals("toolCall"))
                        .map(c -> (ToolCall)c)
                        .toList();

                List<ToolResultMessage> toolResults = new LinkedList<>();
                hasMoreToolCalls = false;
                if (!toolCalls.isEmpty()) {
                    CompletableFuture<ExecutedToolCallBatch> executedToolCallBatch = executeToolCalls(currentContext, assistantMessage, config, signal, emit);
                    toolResults.addAll(executedToolCallBatch.get().getMessages());
                    hasMoreToolCalls = !executedToolCallBatch.get().isTerminate();

                    for (ToolResultMessage toolResult : toolResults) {
                        currentContext.getMessages().add(toolResult);
                        newMessages.add(toolResult);
                    }
                }

                emit.apply(new AgentEvent.TurnEnd(assistantMessage, toolResults));

                PrepareNextTurnContext nextTurnContext = new PrepareNextTurnContext(assistantMessage, toolResults, currentContext, newMessages);
                AgentLoopTurnUpdate nextTurnSnapshot = config.getPrepareNextTurn().apply(nextTurnContext);
                if (nextTurnSnapshot != null) {
                    config = config.copy();
                    currentContext = nextTurnSnapshot.context() == null ? currentContext : nextTurnSnapshot.context();
                    if (nextTurnSnapshot.model() != null) {
                        config.setModel(nextTurnSnapshot.model());
                    }
                    if (nextTurnSnapshot.thinkingLevel() != null) {
                        if (!ThinkingLevel.OFF.equals(nextTurnSnapshot.thinkingLevel())) {
                            config.setReasoning(nextTurnSnapshot.thinkingLevel());
                        }
                    }
                }
                if (config.getShouldStopAfterTurn()
                        .apply(new ShouldStopAfterTurnContext(assistantMessage, toolResults, currentContext, newMessages))) {
                    emit.apply(new AgentEvent.AgentEnd(newMessages));
                    return;
                }

                pendingMessages = config.getSteeringMessage();
            }
            // Agent would stop here. Check for follow-up messages.
            List<AgentMessage> followUpMessages = config.getGetFollowUpMessages().get();
            if (!followUpMessages.isEmpty()) {
                // Set as pending so inner loop processes them
                pendingMessages = followUpMessages;
                continue;
            }

            // No more messages, exit
            break;
        }

        emit.apply(new AgentEvent.AgentEnd(newMessages));
    }

    public CompletableFuture<AssistantMessage> streamAssistantResponse(AgentContext context, AgentLoopConfig config, AbortSignal signal,
                                                                      AgentEventSink emit, StreamFn streamFn) throws ExecutionException, InterruptedException {
        // Apply context transform if configured (AgentMessage[] → AgentMessage[])
        List<AgentMessage> messages = context.getMessages();
        if (config.getTransformContext() != null) {
            messages = config.getTransformContext().apply(messages, signal);
        }

        // Convert to LLM-compatible messages (AgentMessage[] → Message[])
        List<Message> llmMessages = config.getConvertToLlm().apply(messages);
        Context llmContext = new Context(context.getSystemPrompt(), llmMessages, context.getTools());

        StreamFn streamFunction = streamFn == null ? Stream::streamSimple : streamFn;

        // Resolve API key (important for expiring tokens)
        Optional<String> resolvedApiKey = config.getGetApiKey() == null ? config.getGetApiKey().apply(config.getModel().getProvider().value()):Optional.empty();
        if (resolvedApiKey.isEmpty()) {
            resolvedApiKey = Optional.of(config.getApiKey());
        }

        StreamOptions options = new StreamOptions(config);
        options.setApiKey(resolvedApiKey.get());


        AssistantMessageEventStream response = streamFunction.call(config.getModel(), llmContext, options);

        AssistantMessage partialMessage = null;
        boolean addedPartial = false;

        for (AssistantMessageEvent event : response) {
            switch (event) {
                case AssistantMessageEvent.Start e -> {
                    partialMessage = e.partial();
                    context.getMessages().add(partialMessage);
                    addedPartial = true;
                    emit.apply(new AgentEvent.MessageStart(AssistantMessage.copy(partialMessage)));
                }
                case AssistantMessageEvent.TextStart e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.TextDelta e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.TextEnd e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ThinkingStart e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ThinkingDelta e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ThinkingEnd e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ToolCallStart e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ToolCallDelta e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.ToolCallEnd e -> {
                    handleEvent(partialMessage, e, context, emit);
                }
                case AssistantMessageEvent.Done e -> {
                    return handleEndEvent(response, addedPartial, context, emit);
                }
                case AssistantMessageEvent.Error e -> {
                    return handleEndEvent(response, addedPartial, context, emit);
                }
            }
        }

        return handleEndEvent(response, addedPartial, context, emit);
    }

    private void handleEvent(AssistantMessage partialMessage, AssistantMessageEvent event, AgentContext context, AgentEventSink emit) {
        if (partialMessage != null) {
            partialMessage = event.partial();
            context.getMessages().set(context.getMessages().size() -1 , partialMessage);
            emit.apply(new AgentEvent.MessageUpdate(AssistantMessage.copy(partialMessage), event));
        }
    }
    private CompletableFuture<AssistantMessage> handleEndEvent(AssistantMessageEventStream response, boolean addedPartial, AgentContext context, AgentEventSink emit) throws ExecutionException, InterruptedException {
        AssistantMessage finalMessage = response.result().get();
        if (addedPartial) {
            context.getMessages().set(context.getMessages().size() - 1, finalMessage);
        } else {
            context.getMessages().add(finalMessage);
        }
        if (!addedPartial) {
            emit.apply(new AgentEvent.MessageStart(AssistantMessage.copy(finalMessage)));
        }
        emit.apply(new AgentEvent.MessageEnd(finalMessage));
        return CompletableFuture.completedFuture(finalMessage);
    }

    /**
     * Execute tool calls from an assistant message.
     */
    public CompletableFuture<ExecutedToolCallBatch> executeToolCalls(AgentContext currentContext, AssistantMessage assistantMessage, AgentLoopConfig config,
                                                                     AbortSignal signal, AgentEventSink emit) {
        List<ToolCall> toolCalls = assistantMessage
                .content()
                .stream()
                .filter(c -> ContentTypeEnums.TOOL_CALL.getType().equals(c.type()))
                .map(c -> (ToolCall) c)
                .toList();
        boolean hasSequentialToolCall = toolCalls.stream()
                .anyMatch(tc -> findTool(currentContext, tc.name())
                .flatMap(AgentTool::executionMode)
                .filter(ToolExecutionMode.SEQUENTIAL::equals)
                .isPresent());

        if (ToolExecutionMode.SEQUENTIAL.equals(config.getToolExecution()) || hasSequentialToolCall) {
            return executeToolCallsSequential(currentContext, assistantMessage, toolCalls, config, signal, emit);
        }
        return executeToolCallsParallel(currentContext, assistantMessage, toolCalls, config, signal, emit);
    }

    private CompletableFuture<ExecutedToolCallBatch> executeToolCallsSequential(
            AgentContext currentContext, AssistantMessage assistantMessage, List<ToolCall> toolCalls, AgentLoopConfig config,
            AbortSignal signal, AgentEventSink emit) {
        List<FinalizedToolCallOutcome> finalizedCalls = new LinkedList<>();
        List<ToolResultMessage> messages = new LinkedList<>();
        for (ToolCall toolCall : toolCalls) {
            emit.apply(new AgentEvent.ToolExecutionStart(toolCall.id(), toolCall.name(), toolCall.arguments()));

            AgentToolCall agentToolCall = new AgentToolCall(toolCall);
            ToolCallPreparation preparation = prepareToolCall(currentContext, assistantMessage, agentToolCall, config, signal);
            FinalizedToolCallOutcome finalized = null;
            if (preparation instanceof ToolCallPreparation.Immediate(AgentToolResult result, boolean isError)) {
                finalized = new FinalizedToolCallOutcome(agentToolCall, result, isError);
            } else {
                ToolCallPreparation.Prepared prepared = (ToolCallPreparation.Prepared) preparation;
                ExecutedToolCallOutcome executed = executePreparedToolCall(prepared, signal, emit);
                finalized = finalizeExecutedToolCall(currentContext, assistantMessage, prepared, executed, config, signal);
            }

            emitToolExecutionEnd(finalized, emit);
            ToolResultMessage toolResultMessage = createToolResultMessage(finalized);
            emitToolResultMessage(toolResultMessage, emit);
            finalizedCalls.add(finalized);
            messages.add(toolResultMessage);
        }
        return CompletableFuture.completedFuture(new ExecutedToolCallBatch(messages, shouldTerminateToolBatch(finalizedCalls)));

    }

    private boolean shouldTerminateToolBatch(List<FinalizedToolCallOutcome> finalizedCalls) {
        return !finalizedCalls.isEmpty() && finalizedCalls.stream().anyMatch(finalized -> finalized.result().terminate());
    }

    private void emitToolResultMessage(ToolResultMessage toolResultMessage, AgentEventSink emit) {
        emit.apply(new AgentEvent.MessageStart(toolResultMessage));
        emit.apply(new AgentEvent.MessageEnd(toolResultMessage));
    }

    private ToolResultMessage createToolResultMessage(FinalizedToolCallOutcome finalized) {
        return null;
    }

    private void emitToolExecutionEnd(FinalizedToolCallOutcome finalized, AgentEventSink emit) {
        emit.apply(new AgentEvent.ToolExecutionEnd(
                finalized.toolCall().id(),
                finalized.toolCall().name(),
                finalized.result(),
                finalized.isError()
        ));
    }

    private CompletableFuture<ExecutedToolCallBatch> executeToolCallsParallel(
            AgentContext currentContext, AssistantMessage assistantMessage, List<ToolCall> toolCalls, AgentLoopConfig config,
            AbortSignal signal, AgentEventSink emit) {
        // TODO: implement parallel execution
        return CompletableFuture.completedFuture(new ExecutedToolCallBatch(List.of(), false));
    }

    private ToolCallPreparation prepareToolCall(AgentContext currentContext, AssistantMessage assistantMessage, AgentToolCall toolCall, AgentLoopConfig config, AbortSignal signal) {
        AgentTool tool = currentContext.getTools() == null ? null : currentContext.getTools()
                .stream().filter(t -> toolCall.name().equals(t.name()))
                .findFirst()
                .orElse(null);
        if (tool == null) {
            return new ToolCallPreparation.Immediate(createErrorToolResult("Tool " + toolCall.name() + " not found"), true);
        }
        try {
            AgentToolCall preparedToolCall = prepareToolCallArguments(tool, toolCall);
            var validateArgs = ValidationUtils.validateToolArguments(tool, preparedToolCall.toolCall());
            if (config.getBeforeToolCall() != null) {
                BeforeToolCallResult beforeResult = config.getBeforeToolCall().apply(
                        new BeforeToolCallContext(assistantMessage, toolCall, validateArgs, currentContext),
                        signal
                );
                if (beforeResult != null && beforeResult.block()) {
                    String result = beforeResult.reason().isPresent() ? beforeResult.reason().get() :
                            "Tool execution was blocked";
                    return new ToolCallPreparation.Immediate(createErrorToolResult(result), true);
                }
            }
            return new ToolCallPreparation.Prepared(toolCall, tool, validateArgs);
        } catch (Exception e) {
            return new ToolCallPreparation.Immediate(createErrorToolResult(e.getMessage()), true);
        }

    }

    private AgentToolCall prepareToolCallArguments(AgentTool tool, AgentToolCall toolCall) {
        if (tool.prepareArguments().isEmpty()) {
            return toolCall;
        }
        Map<String, Object> preparedArgs = tool.prepareArguments().get().apply(toolCall.arguments());
        if (preparedArgs == toolCall.arguments()) {
            return toolCall;
        }
        return new AgentToolCall(new ToolCall(toolCall.id(), toolCall.name(), preparedArgs, toolCall.thoughtSignature()));
    }

    private AgentToolResult createErrorToolResult(String message) {
        return AgentToolResult.of(List.of(new TextContent(message, Optional.empty())));
    }

    private ExecutedToolCallOutcome executePreparedToolCall(ToolCallPreparation.Prepared prepared, AbortSignal signal, AgentEventSink emit) {
        List<CompletableFuture<Void>> updateEvents = new ArrayList<>();

        try {
            AgentToolResult result = prepared.tool.execute(
                    prepared.toolCall.id(),
                    prepared.args,
                    signal,
                    partialResult -> updateEvents.add(
                            emit.apply(new AgentEvent.ToolExecutionUpdate(
                                    prepared.toolCall.id(),
                                    prepared.toolCall.name(),
                                    prepared.toolCall.arguments(),
                                    partialResult
                            ))
                    )
            ).join();
            CompletableFuture.allOf(updateEvents.toArray(CompletableFuture[]::new)).join();
            return new ExecutedToolCallOutcome(result, false);
        } catch (Throwable e) {
            CompletableFuture.allOf(updateEvents.toArray(CompletableFuture[]::new)).join();
            return new ExecutedToolCallOutcome(createErrorToolResult(e.getMessage()), true);
        }
    }

    private FinalizedToolCallOutcome finalizeExecutedToolCall(AgentContext currentContext, AssistantMessage assistantMessage, ToolCallPreparation.Prepared prepared,
                                                              ExecutedToolCallOutcome executed, AgentLoopConfig config, AbortSignal signal) {
        // TODO: implement
        return new FinalizedToolCallOutcome(prepared.toolCall(), executed.result(), executed.isError());
    }

    private Optional<AgentTool> findTool(AgentContext context, String name) {
        return context.getTools().stream()
                .filter(t -> t.name().equals(name))
                .findFirst();
    }

    private sealed interface ToolCallPreparation {
        record Prepared(AgentToolCall toolCall, AgentTool tool, Map<String, Object> args) implements ToolCallPreparation {}
        record Immediate(AgentToolResult result, boolean isError) implements ToolCallPreparation {}
    }

    private record ExecutedToolCall(ToolResultMessage message, boolean terminate) {}
    private record ExecutedToolCallOutcome(AgentToolResult result, boolean isError) {}

    private record FinalizedToolCallOutcome(AgentToolCall toolCall, AgentToolResult result, boolean isError) {}

}
