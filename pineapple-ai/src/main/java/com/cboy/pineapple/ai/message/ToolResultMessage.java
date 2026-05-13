package com.cboy.pineapple.ai.message;

import com.cboy.pineapple.ai.content.Content;

import java.util.List;
import java.util.Optional;

public record ToolResultMessage(
        String toolCallId,
        String toolName,
        List<Content> content,
        Optional<Object> details,
        boolean isError,
        long timestamp
) implements Message {
    public String role() {
        return "toolResult";
    }
}
