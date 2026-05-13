package com.cboy.pineapple.ai.content;

import java.util.Optional;

public record TextContent(String text, Optional<String> textSignature) implements Content {
    @Override
    public String type() {
        return "text";
    }
}
