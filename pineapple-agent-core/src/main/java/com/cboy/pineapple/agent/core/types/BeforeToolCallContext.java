package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.content.ToolCall;

public record BeforeToolCallContext(
    ToolCall toolCall,
    AgentContext context
) {}
