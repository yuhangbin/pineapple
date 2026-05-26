package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.ai.types.message.ToolResultMessage;

import java.util.List;

public class ExecutedToolCallBatch {

    private List<ToolResultMessage> messages;

    private boolean terminate;

    public ExecutedToolCallBatch(List<ToolResultMessage> messages, boolean terminate) {
        this.messages = messages;
        this.terminate = terminate;
    }

    public List<ToolResultMessage> getMessages() {
        return messages;
    }

    public ExecutedToolCallBatch setMessages(List<ToolResultMessage> messages) {
        this.messages = messages;
        return this;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public ExecutedToolCallBatch setTerminate(boolean terminate) {
        this.terminate = terminate;
        return this;
    }
}
