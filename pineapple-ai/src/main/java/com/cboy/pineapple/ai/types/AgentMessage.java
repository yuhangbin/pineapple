package com.cboy.pineapple.ai.types;

public sealed interface AgentMessage permits Message, ExtensionMessage{
    long timestamp();
}

non-sealed interface ExtensionMessage extends AgentMessage {}
