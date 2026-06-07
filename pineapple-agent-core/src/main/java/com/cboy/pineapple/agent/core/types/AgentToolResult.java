package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.content.ImageContent;
import com.cboy.pineapple.ai.types.content.TextContent;

import java.util.List;
import java.util.Optional;

/**
 * Final or partial result produced by a tool.
 */
public record AgentToolResult(
        List<TextContent> content,
        Optional<Object> details,
        boolean terminate
) {

    public static AgentToolResult of(List<TextContent> content) {
        return new AgentToolResult(content, Optional.empty(), false);
    }

    public static AgentToolResult of(List<TextContent> content, Object details) {
        return new AgentToolResult(content, Optional.of(details), false);
    }
}
