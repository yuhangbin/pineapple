package com.cboy.pineapple.agent.core.harness.message;

import com.cboy.pineapple.ai.message.ExtensionMessage;

import java.util.Optional;

public record BashExecutionMessage(String command, String output, Integer exitCode,
                            boolean cancelled, boolean truncated, Optional<String> fullOutputPath,
                            long timestamp, Boolean excludeFromContext) implements ExtensionMessage {
    public String role() {
        return "bashExecution";
    }
}
