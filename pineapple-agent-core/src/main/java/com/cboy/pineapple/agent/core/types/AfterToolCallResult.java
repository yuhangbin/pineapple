package com.cboy.pineapple.agent.core.types;

import com.cboy.pineapple.ai.types.content.Content;

import java.util.List;
import java.util.Optional;

public record AfterToolCallResult(
    Optional<List<Content>> content,
    Optional<Object> details,
    Optional<Boolean> isError,
    Optional<Boolean> terminate
) {}
