package com.cboy.pineapple.ai.types.message;

public sealed interface AgentMessage permits Message, ExtensionMessage{
    String role();
    long timestamp();
}
