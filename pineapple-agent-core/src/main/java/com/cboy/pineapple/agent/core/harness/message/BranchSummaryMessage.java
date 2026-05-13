package com.cboy.pineapple.agent.core.harness.message;

import com.cboy.pineapple.ai.message.ExtensionMessage;

public record BranchSummaryMessage(String summary, String fromId, long timestamp) implements ExtensionMessage {
    public String role() {
        return "branchSummary";
    }
}
