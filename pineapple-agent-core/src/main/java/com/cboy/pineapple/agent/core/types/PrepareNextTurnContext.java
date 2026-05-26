package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.ai.types.message.AssistantMessage;
import com.cboy.pineapple.ai.types.message.ToolResultMessage;

import java.util.List;

public class PrepareNextTurnContext extends ShouldStopAfterTurnContext{

    public PrepareNextTurnContext(AssistantMessage message, List<ToolResultMessage> toolResults, AgentContext context, List<AgentMessage> newMessages) {
        super(message, toolResults, context, newMessages);
    }
}
