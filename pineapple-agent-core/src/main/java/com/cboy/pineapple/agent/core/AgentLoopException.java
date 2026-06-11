package com.cboy.pineapple.agent.core;

public class AgentLoopException extends RuntimeException {

    public AgentLoopException(String message) {
        super(message);
    }

    public AgentLoopException(String message, Throwable cause) {
        super(message, cause);
    }
}
