package com.cboy.pineapple.agent.core.harness.message;

import com.cboy.pineapple.ai.types.message.ExtensionMessage;

public record CompactionSummaryMessage(String summary, long tokensBefore, long timestamp) implements ExtensionMessage {
    public String role() {
        return "compactionSummary";
    }
}
