package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.content.Content;
import com.cboy.pineapple.ai.types.content.ToolCall;

import java.util.List;
import java.util.Optional;

public record AfterToolCallContext(
    ToolCall toolCall,
    List<Content> content,
    Optional<Object> details,
    boolean isError,
    AgentContext context
) {}
