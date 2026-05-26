package com.cboy.pineapple.ai.types.transport;

import java.util.Map;

public record ProviderResponse(
        int statusCode,
        Map<String, String> headers,
        String body
) {}
