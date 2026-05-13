package com.cboy.pineapple.ai.message;

import com.cboy.pineapple.ai.content.Content;
import com.cboy.pineapple.ai.content.TextContent;

import java.util.List;
import java.util.Optional;

public record UserMessage(List<Content> content, long timestamp) implements Message {
    @Override
    public String role() {
        return "user";
    }

    public static UserMessage of(String text, long timestamp) {
        return new UserMessage(List.of(new TextContent(text, Optional.empty())), timestamp);
    }

    public static UserMessage of(List<Content> blocks, long timestamp) {
        return new UserMessage(blocks, timestamp);
    }

    public boolean isPlainText() {
        return content.size() == 1 && content.getFirst() instanceof TextContent;
    }
}
