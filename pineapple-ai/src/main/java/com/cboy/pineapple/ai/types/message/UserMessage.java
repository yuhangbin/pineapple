package com.cboy.pineapple.ai.types.message;

import com.cboy.pineapple.ai.types.content.Content;
import com.cboy.pineapple.ai.types.content.TextContent;

import java.util.List;
import java.util.Optional;

public record UserMessage(List<Content> content, long timestamp) implements Message {
    @Override
    public String role() {
        return "user";
    }

    public static UserMessage of(String text) {
        return new UserMessage(List.of(new TextContent(text, Optional.empty())), System.currentTimeMillis());
    }

    public static UserMessage of(List<Content> blocks) {
        return new UserMessage(blocks, System.currentTimeMillis());
    }

    public boolean isPlainText() {
        return content.size() == 1 && content.getFirst() instanceof TextContent;
    }
}
