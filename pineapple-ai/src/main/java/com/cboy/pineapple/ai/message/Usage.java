package com.cboy.pineapple.ai.message;

public record Usage(long input, long output, long cacheRead, long cacheWrite, long totalTokens, Cost cost) {}
