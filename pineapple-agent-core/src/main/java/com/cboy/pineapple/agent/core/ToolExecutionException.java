package com.cboy.pineapple.agent.core;

public class ToolExecutionException extends AgentLoopException {

    public ToolExecutionException(String message) {
        super(message);
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
