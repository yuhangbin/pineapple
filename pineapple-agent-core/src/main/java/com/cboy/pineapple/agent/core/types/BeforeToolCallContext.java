package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.message.AssistantMessage;

import java.util.Map;

public record BeforeToolCallContext(
    AssistantMessage assistantMessage,
    AgentToolCall toolCall,
    Map<String, Object> args,
    AgentContext context
) {}
