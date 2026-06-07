package com.cboy.pineapple.agent.core.types;

import java.util.Optional;

/**
 * Result returned from {@code beforeToolCall}.
 *
 * Returning {@code block: true} prevents the tool from executing. The loop emits an error tool result instead.
 * {@code reason} becomes the text shown in that error result. If omitted, a default blocked message is used.
 */
public record BeforeToolCallResult(
    boolean block,
    Optional<String> reason
) {
    public static final BeforeToolCallResult BLOCK = new BeforeToolCallResult(true, Optional.empty());
}
