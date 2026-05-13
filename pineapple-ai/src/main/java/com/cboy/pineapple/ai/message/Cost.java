package com.cboy.pineapple.ai.message;

public record Cost(double input, double output, double cacheRead, double cacheWrite, double total) {}
