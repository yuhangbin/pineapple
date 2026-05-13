package com.cboy.pineapple.ai.content;

public sealed interface Content permits ImageContent, TextContent, ThinkingContent, ToolCall {
    String type();
}
