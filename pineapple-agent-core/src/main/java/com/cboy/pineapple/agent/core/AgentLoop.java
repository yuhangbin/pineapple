package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.agent.core.types.*;
import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.ThinkingLevel;
import com.cboy.pineapple.ai.types.content.Content;
import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.ai.types.message.AssistantMessage;
import com.cboy.pineapple.ai.types.message.StopReason;
import com.cboy.pineapple.ai.types.message.ToolResultMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class AgentLoop {


    public void runLoop(AgentContext currentContext, List<AgentMessage> newMessages, AgentLoopConfig initialConfig,
                          AbortSignal signal, AgentEventSink emit, StreamFn streamFn) throws ExecutionException, InterruptedException {
        boolean firstTurn = true;
        // Check for steering messages at start (user may have typed while waiting)
        List<AgentMessage> pendingMessages = initialConfig.getSteeringMessage();

        // Outer loop: continues when queued follow-up messages arrive after agent would stop
        while (true) {
            boolean hasMoreToolCalls = true;

            // Inner loop: process tool calls and steering messages
            while (hasMoreToolCalls || pendingMessages.size() > 0) {
                if (!firstTurn) {
                    emit.apply(new AgentEvent.TurnStart());
                } else {
                    firstTurn = false;
                }

                // Process pending messages (inject before next assistant response)
                if (pendingMessages.size() > 0) {
                    for (AgentMessage pendingMessage : pendingMessages) {
                        emit.apply(new AgentEvent.MessageStart(pendingMessage));
                        emit.apply(new AgentEvent.MessageEnd(pendingMessage));
                        currentContext.getMessages().add(pendingMessage);
                        newMessages.add(pendingMessage);
                    }
                    pendingMessages = new LinkedList<>();
                }

                // Stream assistant response
                CompletableFuture<AssistantMessage> message = streamAssistantResponse(currentContext, initialConfig, signal, emit, streamFn);
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
                    CompletableFuture<ExecutedToolCallBatch> executedToolCallBatch = executeToolCalls(currentContext, assistantMessage, initialConfig, signal, emit);
                    toolResults.addAll(executedToolCallBatch.get().getMessages());
                    hasMoreToolCalls = !executedToolCallBatch.get().isTerminate();

                    for (ToolResultMessage toolResult : toolResults) {
                        currentContext.getMessages().add(toolResult);
                        newMessages.add(toolResult);
                    }
                }

                emit.apply(new AgentEvent.TurnEnd(assistantMessage, toolResults));

                PrepareNextTurnContext nextTurnContext = new PrepareNextTurnContext(assistantMessage, toolResults, currentContext, newMessages);
                AgentLoopTurnUpdate nextTurnSnapshot = initialConfig.getPrepareNextTurn().apply(nextTurnContext);
                if (nextTurnSnapshot != null) {
                    currentContext = nextTurnSnapshot.context() == null ? currentContext : nextTurnSnapshot.context();
                    if (nextTurnSnapshot.model() != null) {
                        initialConfig.setModel(nextTurnSnapshot.model());
                    }
                    if (nextTurnSnapshot.thinkingLevel() != null) {
                        if (!ThinkingLevel.OFF.equals(nextTurnSnapshot.thinkingLevel())) {
                            initialConfig.setReasoning(nextTurnSnapshot.thinkingLevel());
                        }
                    }
                }
                if (initialConfig.getShouldStopAfterTurn()
                        .apply(new ShouldStopAfterTurnContext(assistantMessage, toolResults, currentContext, newMessages))) {
                    emit.apply(new AgentEvent.AgentEnd(newMessages));
                    return;
                }

                pendingMessages = initialConfig.getSteeringMessage();
            }
            // Agent would stop here. Check for follow-up messages.
            List<AgentMessage> followUpMessages = initialConfig.getGetFollowUpMessages().get();
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
                                                                      AgentEventSink emit, StreamFn streamFn) {
        return null;
    }

    public CompletableFuture<ExecutedToolCallBatch> executeToolCalls(AgentContext context, AssistantMessage assistantMessage, AgentLoopConfig config,
                                                                     AbortSignal signal, AgentEventSink emit) {
        return null;
    }
}
