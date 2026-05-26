package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.SimpleStreamOptions;
import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.ai.types.message.Message;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AgentLoopConfig extends SimpleStreamOptions {

    Model<Api> model;

    /**
     * Converts AgentMessage[] to LLM-compatible Message[] before each LLM call.
     *
     * Each AgentMessage must be converted to a UserMessage, AssistantMessage, or ToolResultMessage
     * that the LLM can understand. AgentMessages that cannot be converted (e.g., UI-only notifications,
     * status messages) should be filtered out.
     *
     * Contract: must not throw. Return a safe fallback value instead.
     */
    Function<List<AgentMessage>, List<Message>> convertToLlm;

    /**
     * Optional transform applied to the context before convertToLlm.
     *
     * Use this for operations that work at the AgentMessage level:
     * - Context window management (pruning old messages)
     * - Injecting context from external sources
     *
     * The hook receives the agent abort signal and is responsible for honoring it.
     *
     * Contract: must not throw. Return the original messages or another safe fallback value instead.
     */
    TransformContextFunction transformContext;

    /**
     * Resolves an API key dynamically for each LLM call.
     *
     * Useful for short-lived OAuth tokens (e.g., GitHub Copilot) that may expire
     * during long-running tool execution phases.
     *
     * Contract: must not throw. Return empty when no key is available.
     */
    Function<String, Optional<String>> getApiKey;

    /**
     * Called after each turn fully completes and turn_end has been emitted.
     *
     * If it returns true, the loop emits agent_end and exits before polling steering or follow-up queues,
     * without starting another LLM call. The current assistant response and any tool executions finish normally.
     *
     * Use this to request a graceful stop after the current turn, e.g. before context gets too full.
     *
     * Contract: must not throw. Throwing interrupts the low-level agent loop without producing a normal event sequence.
     */
    Function<ShouldStopAfterTurnContext, Boolean> shouldStopAfterTurn;

    /**
     * Called after turn_end and before the loop decides whether another provider request should start.
     * Return replacement context/model/thinking state to affect the next turn in this run.
     * Return null to keep using the current context/config.
     */
    Function<PrepareNextTurnContext, AgentLoopTurnUpdate> prepareNextTurn;

    /**
     * Returns steering messages to inject into the conversation mid-run.
     *
     * Called after the current assistant turn finishes executing its tool calls, unless shouldStopAfterTurn exits first.
     * If messages are returned, they are added to the context before the next LLM call.
     * Tool calls from the current assistant message are not skipped.
     *
     * Contract: must not throw. Return empty list when no steering messages are available.
     */
    Supplier<List<AgentMessage>> getSteeringMessages;

    /**
     * Returns follow-up messages to process after the agent would otherwise stop.
     *
     * Called when the agent has no more tool calls and no steering messages.
     * If messages are returned, they're added to the context and the agent
     * continues with another turn.
     *
     * Contract: must not throw. Return empty list when no follow-up messages are available.
     */
    Supplier<List<AgentMessage>> getFollowUpMessages;

    /**
     * Tool execution mode.
     * - SEQUENTIAL: execute tool calls one by one
     * - PARALLEL: preflight tool calls sequentially, then execute allowed tools concurrently
     *
     * Default: PARALLEL
     */
    ToolExecutionMode toolExecution;

    /**
     * Called before a tool is executed, after arguments have been validated.
     *
     * Return a non-null result to prevent execution. The loop emits an error tool result instead.
     */
    Function<BeforeToolCallContext, BeforeToolCallResult> beforeToolCall;

    /**
     * Called after a tool finishes executing, before tool_execution_end and tool-result message events are emitted.
     *
     * Return a non-null result to override parts of the executed tool result.
     * Any absent fields keep their original values. No deep merge is performed.
     */
    Function<AfterToolCallContext, AfterToolCallResult> afterToolCall;

    public List<AgentMessage> getSteeringMessage() {
        return getSteeringMessages != null ? getSteeringMessages.get() : Collections.emptyList();
    }

    @FunctionalInterface
    public interface TransformContextFunction {
        List<AgentMessage> apply(List<AgentMessage> messages, AbortSignal signal);
    }

    public Model<Api> getModel() {
        return model;
    }

    public Function<List<AgentMessage>, List<Message>> getConvertToLlm() {
        return convertToLlm;
    }

    public TransformContextFunction getTransformContext() {
        return transformContext;
    }

    public Function<String, Optional<String>> getGetApiKey() {
        return getApiKey;
    }

    public Function<ShouldStopAfterTurnContext, Boolean> getShouldStopAfterTurn() {
        return shouldStopAfterTurn;
    }

    public Function<PrepareNextTurnContext, AgentLoopTurnUpdate> getPrepareNextTurn() {
        return prepareNextTurn;
    }

    public Supplier<List<AgentMessage>> getGetSteeringMessages() {
        return getSteeringMessages;
    }

    public Supplier<List<AgentMessage>> getGetFollowUpMessages() {
        return getFollowUpMessages;
    }

    public ToolExecutionMode getToolExecution() {
        return toolExecution;
    }

    public Function<BeforeToolCallContext, BeforeToolCallResult> getBeforeToolCall() {
        return beforeToolCall;
    }

    public Function<AfterToolCallContext, AfterToolCallResult> getAfterToolCall() {
        return afterToolCall;
    }

    public AgentLoopConfig setModel(Model<Api> model) {
        this.model = model;
        return this;
    }

    public AgentLoopConfig setConvertToLlm(Function<List<AgentMessage>, List<Message>> convertToLlm) {
        this.convertToLlm = convertToLlm;
        return this;
    }

    public AgentLoopConfig setTransformContext(TransformContextFunction transformContext) {
        this.transformContext = transformContext;
        return this;
    }

    public AgentLoopConfig setGetApiKey(Function<String, Optional<String>> getApiKey) {
        this.getApiKey = getApiKey;
        return this;
    }

    public AgentLoopConfig setShouldStopAfterTurn(Function<ShouldStopAfterTurnContext, Boolean> shouldStopAfterTurn) {
        this.shouldStopAfterTurn = shouldStopAfterTurn;
        return this;
    }

    public AgentLoopConfig setPrepareNextTurn(Function<PrepareNextTurnContext, AgentLoopTurnUpdate> prepareNextTurn) {
        this.prepareNextTurn = prepareNextTurn;
        return this;
    }

    public AgentLoopConfig setGetSteeringMessages(Supplier<List<AgentMessage>> getSteeringMessages) {
        this.getSteeringMessages = getSteeringMessages;
        return this;
    }

    public AgentLoopConfig setGetFollowUpMessages(Supplier<List<AgentMessage>> getFollowUpMessages) {
        this.getFollowUpMessages = getFollowUpMessages;
        return this;
    }

    public AgentLoopConfig setToolExecution(ToolExecutionMode toolExecution) {
        this.toolExecution = toolExecution;
        return this;
    }

    public AgentLoopConfig setBeforeToolCall(Function<BeforeToolCallContext, BeforeToolCallResult> beforeToolCall) {
        this.beforeToolCall = beforeToolCall;
        return this;
    }

    public AgentLoopConfig setAfterToolCall(Function<AfterToolCallContext, AfterToolCallResult> afterToolCall) {
        this.afterToolCall = afterToolCall;
        return this;
    }
}
