package com.cboy.pineapple.agent.core.types;

public record BeforeToolCallResult(
    boolean block
) {
    public static final BeforeToolCallResult BLOCK = new BeforeToolCallResult(true);
}
