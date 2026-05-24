package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.ai.types.message.ToolResultMessage;

import java.util.List;

/**
 * Events emitted by the Agent for UI updates.
 * <p>
 * {@code agent_end} is the last event emitted for a run, but awaited {@code Agent.subscribe()}
 * listeners for that event are still part of run settlement. The agent becomes
 * idle only after those listeners finish.
 */
public sealed interface AgentEvent
        permits
        AgentEvent.AgentStart,
        AgentEvent.AgentEnd,
        AgentEvent.TurnStart,
        AgentEvent.TurnEnd,
        AgentEvent.MessageStart,
        AgentEvent.MessageUpdate,
        AgentEvent.MessageEnd,
        AgentEvent.ToolExecutionStart,
        AgentEvent.ToolExecutionUpdate,
        AgentEvent.ToolExecutionEnd {

    String type();

    // ── Agent lifecycle ──────────────────────────────────────────────

    record AgentStart() implements AgentEvent {
        @Override
        public String type() { return "agent_start"; }
    }

    record AgentEnd(List<AgentMessage> messages) implements AgentEvent {
        @Override
        public String type() { return "agent_end"; }
    }

    // ── Turn lifecycle (one assistant response + any tool calls/results) ─

    record TurnStart() implements AgentEvent {
        @Override
        public String type() { return "turn_start"; }
    }

    record TurnEnd(AgentMessage message, List<ToolResultMessage> toolResults) implements AgentEvent {
        @Override
        public String type() { return "turn_end"; }
    }

    // ── Message lifecycle (user, assistant, and toolResult messages) ──

    record MessageStart(AgentMessage message) implements AgentEvent {
        @Override
        public String type() { return "message_start"; }
    }

    record MessageUpdate(AgentMessage message, AssistantMessageEvent assistantMessageEvent) implements AgentEvent {
        @Override
        public String type() { return "message_update"; }
    }

    record MessageEnd(AgentMessage message) implements AgentEvent {
        @Override
        public String type() { return "message_end"; }
    }

    // ── Tool execution lifecycle ─────────────────────────────────────

    record ToolExecutionStart(String toolCallId, String toolName, Object args) implements AgentEvent {
        @Override
        public String type() { return "tool_execution_start"; }
    }

    record ToolExecutionUpdate(String toolCallId, String toolName, Object args, Object partialResult) implements AgentEvent {
        @Override
        public String type() { return "tool_execution_update"; }
    }

    record ToolExecutionEnd(String toolCallId, String toolName, Object result, boolean isError) implements AgentEvent {
        @Override
        public String type() { return "tool_execution_end"; }
    }
}
