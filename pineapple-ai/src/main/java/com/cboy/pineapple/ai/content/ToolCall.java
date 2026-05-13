package com.cboy.pineapple.ai.content;

import java.util.Map;
import java.util.Optional;

public record ToolCall(String id, String name, Map<String, Object> arguments, Optional<String> thoughtSignature) implements Content {
    @Override
    public String type() {
        return "toolCall";
    }
}
