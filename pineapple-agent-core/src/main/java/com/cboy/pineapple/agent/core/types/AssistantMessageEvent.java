package com.cboy.pineapple.agent.core.types;

/**
 * Streaming events emitted during an assistant message response.
 * Only emitted for assistant messages during streaming.
 */
public sealed interface AssistantMessageEvent
        permits
        AssistantMessageEvent.ContentBlockStart,
        AssistantMessageEvent.ContentBlockDelta,
        AssistantMessageEvent.ContentBlockStop,
        AssistantMessageEvent.MessageDelta {

    String type();

    record ContentBlockStart(int index, Object contentBlock) implements AssistantMessageEvent {
        @Override
        public String type() { return "content_block_start"; }
    }

    record ContentBlockDelta(int index, Object delta) implements AssistantMessageEvent {
        @Override
        public String type() { return "content_block_delta"; }
    }

    record ContentBlockStop(int index) implements AssistantMessageEvent {
        @Override
        public String type() { return "content_block_stop"; }
    }

    record MessageDelta(Object delta, Object usage) implements AssistantMessageEvent {
        @Override
        public String type() { return "message_delta"; }
    }
}
