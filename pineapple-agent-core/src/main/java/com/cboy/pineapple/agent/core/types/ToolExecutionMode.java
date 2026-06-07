package com.cboy.pineapple.agent.core.types;

public enum ToolExecutionMode {
    SEQUENTIAL("sequential"),
    PARALLEL("parallel")
    ;
    private final String mode;

    ToolExecutionMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
