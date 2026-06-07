package com.cboy.pineapple.ai.utils;

import com.cboy.pineapple.ai.types.message.AssistantMessage;

/**
 * Event stream for assistant message streaming events.
 * Completes when a {@code Done} or {@code Error} event is pushed.
 */
public class AssistantMessageEventStream extends EventStream<AssistantMessageEvent, AssistantMessage> {

    public AssistantMessageEventStream() {
        super(
                event -> "done".equals(event.type()) || "error".equals(event.type()),
                event -> {
                    if (event instanceof AssistantMessageEvent.Done(var reason, AssistantMessage message)) {
                        return message;
                    } else if (event instanceof AssistantMessageEvent.Error(var reason, AssistantMessage message)) {
                        return message;
                    }
                    throw new IllegalStateException("Unexpected event type for final result: " + event.type());
                }
        );
    }

    public static AssistantMessageEventStream createAssistantMessageEventStream() {
        return new AssistantMessageEventStream();
    }
}
