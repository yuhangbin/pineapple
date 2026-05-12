package com.cboy.pineapple.ai.types;

import java.util.Map;
import java.util.Optional;

sealed interface Content permits ImageContent, TextContent, ThinkingContent, ToolCall {
    String type();
}
record TextContent(String text, Optional<String> textSignature) implements Content {
    @Override
    public String type() {
        return "text";
    }
}
record ImageContent(String data, String mimeType) implements Content {
    @Override
    public String type() {
        return "image";
    }
}
/** When redacted = true, the thinking content was redacted by safety filters. The opaque
 *  encrypted payload is stored in `thinkingSignature` so it can be passed back
 *  to the API for multi-turn continuity. */
record ThinkingContent (String thinking, Optional<String> thinkingSignature, boolean redacted) implements Content {
    @Override
    public String type() {
        return "thinking";
    }
}

record ToolCall (String id, String name, Map<String, Object> arguments, Optional<String> thoughtSignature) implements Content {
    @Override
    public String type() {
        return "toolCall";
    }
}
