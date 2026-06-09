package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.content.Content;
import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.message.AssistantMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Context passed to `afterToolCall`. */
public record AfterToolCallContext(
    AssistantMessage assistantMessage,
    AgentToolCall toolCall,
    Map<String, Object> args,
    AgentToolResult result,
    boolean isError,
    AgentContext context
) {}
