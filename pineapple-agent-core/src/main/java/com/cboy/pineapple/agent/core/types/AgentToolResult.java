package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.content.Content;

import java.util.List;
import java.util.Optional;

/**
 * Final or partial result produced by a tool.
 */
public record AgentToolResult(
        List<Content> content,
        Optional<Object> details,
        boolean terminate
) {

    public static AgentToolResult of(List<? extends Content> content) {
        return new AgentToolResult(List.copyOf(content), Optional.empty(), false);
    }

    public static AgentToolResult of(List<? extends Content> content, Object details) {
        return new AgentToolResult(List.copyOf(content), Optional.of(details), false);
    }
}
