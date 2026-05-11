package com.cboy.pineapple.agent.core.types;

import java.util.List;
import java.util.Optional;

enum StopReason { STOP, LENGTH, TOOL_USE, ERROR, ABORTED }

record Usage(long input, long output, long cacheRead, long cacheWrite) {}


sealed interface Message extends AgentMessage permits AssistantMessage, ToolResultMessage, UserMessage {
    String role();
    long timestamp();
}

record UserMessage(String content, long timestamp) implements Message {
    @Override
    public String role() {
        return "user";
    }
}

record AssistantMessage(List<ContentBlock> content,
                        String api,
                        String provider,
                        String model,
                        Usage usage,
                        StopReason stopReason,
                        Optional<String> errorMessage,
                        long timestamp) implements Message {
    public String role() {
        return "toolResult";
    }
}



record ToolResultMessage(
        String toolCallId,
        String toolName,
        List<ContentBlock> content,
        Object details,
        boolean isError,
        long timestamp
) implements Message {
    public String role() {
        return "toolResult";
    }
}


