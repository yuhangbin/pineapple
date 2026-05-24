package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.agent.core.types.AgentEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@FunctionalInterface
public interface AgentEventSink extends Function<AgentEvent, CompletableFuture<Void>> {
}
