package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.agent.core.types.context.AgentContext;
import com.cboy.pineapple.ai.types.ThinkingLevel;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;

/** Replacement runtime state used by the agent loop before starting another provider request. */
public record AgentLoopTurnUpdate(
        AgentContext context, Model<Api> model, ThinkingLevel thinkingLevel
) {
}
