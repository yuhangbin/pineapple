package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.ai.types.Context;
import com.cboy.pineapple.ai.types.StreamOptions;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;
import com.cboy.pineapple.ai.utils.AssistantMessageEventStream;

/**
 * Stream function used by the agent loop.
 *
 * Contract:
 * - Must not throw for request/model/runtime failures.
 * - Must return an AssistantMessageEventStream.
 * - Failures must be encoded in the returned stream via protocol events and a
 *   final AssistantMessage with stopReason "error" or "aborted" and errorMessage.
 */
@FunctionalInterface
public interface StreamFn {
    AssistantMessageEventStream call(Model<Api> model, Context context, StreamOptions options);
}
