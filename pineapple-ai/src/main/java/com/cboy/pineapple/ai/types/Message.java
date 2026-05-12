package com.cboy.pineapple.ai.types;

import com.cboy.pineapple.ai.utils.AssistantMessageDiagnostic;

import java.util.List;
import java.util.Optional;

enum StopReason { STOP, LENGTH, TOOL_USE, ERROR, ABORTED }

record Usage(long input, long output, long cacheRead, long cacheWrite, long totalTokens, Cost cost) {}

record Cost(double input, double output, double cacheRead, double cacheWrite, double total) {}


sealed public interface Message extends AgentMessage permits AssistantMessage, ToolResultMessage, UserMessage {
    String role();
    long timestamp();
}

record UserMessage(List<Content> content, long timestamp) implements Message {
    @Override
    public String role() {
        return "user";
    }

    public static UserMessage of(String text, long timestamp) {
        return new UserMessage(List.of(new TextContent(text, Optional.empty())), timestamp);
    }

    public static UserMessage of(List<Content> blocks, long timestamp) {
        return new UserMessage(blocks, timestamp);
    }

    public boolean isPlainText() {
        return content.size() == 1 && content.getFirst() instanceof TextContent;
    }
}

record AssistantMessage(List<Content> content,
                        Api api,
                        Provider provider,
                        String model,
                        Optional<String> responseModel,
                        Optional<String> responseId,
                        Optional<List<AssistantMessageDiagnostic>> diagnostics,
                        Usage usage,
                        StopReason stopReason,
                        Optional<String> errorMessage,
                        long timestamp) implements Message {
    public String role() {
        return "assistant";
    }

    public boolean hasToolCalls() {
        return content.stream().anyMatch(c -> c instanceof ToolCall);
    }

}



record ToolResultMessage(
        String toolCallId,
        String toolName,
        List<Content> content,
        Optional<Object> details,
        boolean isError,
        long timestamp
) implements Message {
    public String role() {
        return "toolResult";
    }

}


