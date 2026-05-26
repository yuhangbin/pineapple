package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.ai.types.message.AssistantMessage;
import com.cboy.pineapple.ai.types.message.ToolResultMessage;

import java.util.List;

/** Context passed to `shouldStopAfterTurn`. */
public class ShouldStopAfterTurnContext {

    /** The assistant message that completed the turn. */
    private AssistantMessage message;

    /** Tool result messages passed to the preceding `turn_end` event. */
    private List<ToolResultMessage> toolResults;

    /** Current agent context after the turn's assistant message and tool results have been appended. */
    private AgentContext context;

    /** Messages that this loop invocation will return if it exits at this point. Prompt runs include the initial prompt messages; continuation runs do not include pre-existing context messages. */
    private List<AgentMessage> newMessages;

    public ShouldStopAfterTurnContext(AssistantMessage message, List<ToolResultMessage> toolResults, AgentContext context, List<AgentMessage> newMessages) {
        this.message = message;
        this.toolResults = toolResults;
        this.context = context;
        this.newMessages = newMessages;
    }
}
