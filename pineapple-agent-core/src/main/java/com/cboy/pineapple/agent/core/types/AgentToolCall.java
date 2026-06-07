package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.content.ToolCall;

import java.util.Map;
import java.util.Optional;

/**
 * A single tool call content block emitted by an assistant message.
 * Agent-core view of {@link ToolCall}.
 */
public record AgentToolCall(ToolCall toolCall) {

    public String id() {
        return toolCall.id();
    }

    public String name() {
        return toolCall.name();
    }

    public Map<String, Object> arguments() {
        return toolCall.arguments();
    }

    public Optional<String> thoughtSignature() {
        return toolCall.thoughtSignature();
    }

    public static AgentToolCall from(ToolCall toolCall) {
        return new AgentToolCall(toolCall);
    }

    public static ToolCall toolCall(AgentToolCall agentToolCall) {
        return new ToolCall(agentToolCall.id(), agentToolCall.name(), agentToolCall.arguments(), agentToolCall.thoughtSignature());
    }
}
