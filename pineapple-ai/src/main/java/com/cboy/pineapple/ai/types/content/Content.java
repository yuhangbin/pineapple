package com.cboy.pineapple.ai.types.content;

public sealed interface Content permits ImageContent, TextContent, ThinkingContent, ToolCall {
    String type();
}
