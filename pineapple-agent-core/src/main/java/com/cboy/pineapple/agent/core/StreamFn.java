package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.agent.core.types.AgentLoopConfig;
import com.cboy.pineapple.ai.types.Context;
import com.cboy.pineapple.ai.types.message.AssistantMessage;

@FunctionalInterface
public interface StreamFn {
    AssistantMessage call(Context context, AgentLoopConfig config);
}
