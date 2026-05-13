package com.cboy.pineapple.ai.message;

import com.cboy.pineapple.ai.content.Content;
import com.cboy.pineapple.ai.content.ToolCall;
import com.cboy.pineapple.ai.diagnostic.AssistantMessageDiagnostic;
import com.cboy.pineapple.ai.model.Api;
import com.cboy.pineapple.ai.model.Provider;

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
}
