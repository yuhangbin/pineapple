package com.cboy.pineapple.ai.types;

import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;
import com.cboy.pineapple.ai.types.transport.ProviderResponse;
import com.cboy.pineapple.ai.types.transport.Transport;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiConsumer;

public class StreamOptions {

    private Float temperature;
    private Integer maxTokens;
    private String apiKey;
    private Transport transport;
    private CacheRetention cacheRetention;
    private String sessionId;
    private BiFunction<Object, Model<? extends Api>, Object> onPayload;
    private BiConsumer<ProviderResponse, Model<? extends Api>> onResponse;
    private Map<String, String> headers;
    private Integer timeoutMs;
    private Integer maxRetries;
    private Integer maxRetryDelayMs;
    private Map<String, Object> metadata;
}
