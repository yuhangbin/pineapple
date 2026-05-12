package com.cboy.pineapple.agent.core.harness.types;


import com.cboy.pineapple.ai.types.Content;
import com.cboy.pineapple.ai.types.ExtensionMessage;

import java.util.List;
import java.util.Optional;

record BashExecutionMessage(String command, String output, Integer exitCode,
                            boolean cancelled, boolean truncated, Optional<String> fullOutputPath,
                            long timestamp, Boolean excludeFromContext) implements ExtensionMessage {
    public String role() {
        return "bashExecution";
    }
}

record CustomMessage(String customType, List<Content> content, boolean display,
                     Optional<Object> details, long timestamp) implements ExtensionMessage {
    public String role() {
        return "custom";
    }
}

record BranchSummaryMessage(String summary, String fromId, long timestamp) implements ExtensionMessage {
    public String role() {
        return "branchSummary";
    }
}

record CompactionSummaryMessage(String summary, long tokensBefore, long timestamp) implements ExtensionMessage {
    public String role() {
        return "compactionSummary";
    }
}