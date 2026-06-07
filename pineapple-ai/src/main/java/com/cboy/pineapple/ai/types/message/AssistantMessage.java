package com.cboy.pineapple.ai.types.message;

import com.cboy.pineapple.ai.types.content.Content;
import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.diagnostic.AssistantMessageDiagnostic;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Provider;

import java.util.List;
import java.util.Optional;

public record AssistantMessage(List<Content> content,
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

    public static AssistantMessage copy(AssistantMessage other) {
        return new AssistantMessage(
                other.content,
                other.api,
                other.provider,
                other.model,
                other.responseModel,
                other.responseId,
                other.diagnostics,
                other.usage,
                other.stopReason,
                other.errorMessage,
                other.timestamp);
    }
}
