package com.cboy.pineapple.ai.content;

public record ImageContent(String data, String mimeType) implements Content {
    @Override
    public String type() {
        return "image";
    }
}
