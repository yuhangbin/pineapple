package com.cboy.pineapple.ai.utils;

import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.message.AssistantMessage;
import com.cboy.pineapple.ai.types.message.StopReason;

/**
 * Event protocol for AssistantMessageEventStream.
 *
 * Streams should emit {@code start} before partial updates, then terminate with either:
 * - {@code done} carrying the final successful AssistantMessage, or
 * - {@code error} carrying the final AssistantMessage with stopReason "error" or "aborted"
 *   and errorMessage.
 */
public sealed interface AssistantMessageEvent
        permits
        AssistantMessageEvent.Start,
        AssistantMessageEvent.TextStart,
        AssistantMessageEvent.TextDelta,
        AssistantMessageEvent.TextEnd,
        AssistantMessageEvent.ThinkingStart,
        AssistantMessageEvent.ThinkingDelta,
        AssistantMessageEvent.ThinkingEnd,
        AssistantMessageEvent.ToolCallStart,
        AssistantMessageEvent.ToolCallDelta,
        AssistantMessageEvent.ToolCallEnd,
        AssistantMessageEvent.Done,
        AssistantMessageEvent.Error {

    String type();

    default AssistantMessage partial() { return null; }

    record Start(AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "start"; }
    }

    record TextStart(int contentIndex, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "text_start"; }
    }

    record TextDelta(int contentIndex, String delta, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "text_delta"; }
    }

    record TextEnd(int contentIndex, String content, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "text_end"; }
    }

    record ThinkingStart(int contentIndex, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "thinking_start"; }
    }

    record ThinkingDelta(int contentIndex, String delta, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "thinking_delta"; }
    }

    record ThinkingEnd(int contentIndex, String content, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "thinking_end"; }
    }

    record ToolCallStart(int contentIndex, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "toolcall_start"; }
    }

    record ToolCallDelta(int contentIndex, String delta, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "toolcall_delta"; }
    }

    record ToolCallEnd(int contentIndex, ToolCall toolCall, AssistantMessage partial) implements AssistantMessageEvent {
        @Override
        public String type() { return "toolcall_end"; }
    }

    record Done(StopReason reason, AssistantMessage message) implements AssistantMessageEvent {
        @Override
        public String type() { return "done"; }
    }

    record Error(StopReason reason, AssistantMessage error) implements AssistantMessageEvent {
        @Override
        public String type() { return "error"; }
    }
}
