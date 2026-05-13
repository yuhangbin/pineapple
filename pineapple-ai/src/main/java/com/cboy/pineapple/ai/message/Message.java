package com.cboy.pineapple.ai.message;

public sealed interface Message extends AgentMessage permits AssistantMessage, ToolResultMessage, UserMessage {
    String role();
    long timestamp();
}
