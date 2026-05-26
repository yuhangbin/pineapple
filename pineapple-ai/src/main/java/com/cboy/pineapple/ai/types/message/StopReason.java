package com.cboy.pineapple.ai.types.message;

public enum StopReason {
    STOP("stop"), LENGTH("length"), TOOL_USE("tool_use"), ERROR("error"), ABORTED("aborted")

    ;

    private final String reason;

    StopReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
