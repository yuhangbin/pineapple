package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.AbortSignal;
import com.cboy.pineapple.ai.types.tool.Tool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tool definition used by the agent runtime.
 * <p>
 * Extends {@link Tool} with execution capabilities: label for UI display,
 * optional argument preparation, execution logic, and execution mode override.
 */
public interface AgentTool extends Tool {

    /** Human-readable label for UI display. */
    String label();

    /**
     * Optional compatibility shim for raw tool-call arguments before schema validation.
     * Must return an object that matches the tool's parameter schema.
     * Return empty to skip argument preparation.
     */
    default Optional<Function<Map<String, Object>, Map<String, Object>>> prepareArguments() {
        return Optional.empty();
    }

    /**
     * Execute the tool call. Throw on failure instead of encoding errors in content.
     *
     * @param toolCallId the tool call identifier
     * @param params     validated parameters matching the tool's schema
     * @param signal     abort signal for cancellation
     * @param onUpdate   optional callback for progress updates
     * @return future resolving to the tool execution result
     */
    CompletableFuture<AgentToolResult> execute(
            String toolCallId,
            Map<String, Object> params,
            AbortSignal signal,
            Consumer<Object> onUpdate);

    /**
     * Per-tool execution mode override.
     * If empty, the default execution mode applies.
     */
    default Optional<ToolExecutionMode> executionMode() {
        return Optional.empty();
    }
}
