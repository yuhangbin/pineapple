package com.cboy.pineapple.ai.message;

public sealed interface AgentMessage permits Message, ExtensionMessage{
    long timestamp();
}
