package com.cboy.pineapple.ai.content;

import java.util.Optional;

/** When redacted = true, the thinking content was redacted by safety filters. The opaque
 *  encrypted payload is stored in `thinkingSignature` so it can be passed back
 *  to the API for multi-turn continuity. */
public record ThinkingContent(String thinking, Optional<String> thinkingSignature, boolean redacted) implements Content {
    @Override
    public String type() {
        return "thinking";
    }
}
