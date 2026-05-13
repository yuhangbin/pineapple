package com.cboy.pineapple.agent.core.harness.message;

import com.cboy.pineapple.ai.message.ExtensionMessage;
import com.cboy.pineapple.ai.content.Content;

import java.util.List;
import java.util.Optional;

public record CustomMessage(String customType, List<Content> content, boolean display,
                     Optional<Object> details, long timestamp) implements ExtensionMessage {
    public String role() {
        return "custom";
    }
}
