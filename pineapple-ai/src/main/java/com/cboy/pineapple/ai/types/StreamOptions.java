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
    private AbortSignal signal;
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

    public StreamOptions() {}

    public StreamOptions(StreamOptions other) {
        this.temperature = other.temperature;
        this.maxTokens = other.maxTokens;
        this.signal = other.signal;
        this.apiKey = other.apiKey;
        this.transport = other.transport;
        this.cacheRetention = other.cacheRetention;
        this.sessionId = other.sessionId;
        this.onPayload = other.onPayload;
        this.onResponse = other.onResponse;
        this.headers = other.headers;
        this.timeoutMs = other.timeoutMs;
        this.maxRetries = other.maxRetries;
        this.maxRetryDelayMs = other.maxRetryDelayMs;
        this.metadata = other.metadata;
    }

    public Float getTemperature() {
        return temperature;
    }

    public StreamOptions setTemperature(Float temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public StreamOptions setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public AbortSignal getSignal() {
        return signal;
    }

    public StreamOptions setSignal(AbortSignal signal) {
        this.signal = signal;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public StreamOptions setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public Transport getTransport() {
        return transport;
    }

    public StreamOptions setTransport(Transport transport) {
        this.transport = transport;
        return this;
    }

    public CacheRetention getCacheRetention() {
        return cacheRetention;
    }

    public StreamOptions setCacheRetention(CacheRetention cacheRetention) {
        this.cacheRetention = cacheRetention;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public StreamOptions setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public BiFunction<Object, Model<? extends Api>, Object> getOnPayload() {
        return onPayload;
    }

    public StreamOptions setOnPayload(BiFunction<Object, Model<? extends Api>, Object> onPayload) {
        this.onPayload = onPayload;
        return this;
    }

    public BiConsumer<ProviderResponse, Model<? extends Api>> getOnResponse() {
        return onResponse;
    }

    public StreamOptions setOnResponse(BiConsumer<ProviderResponse, Model<? extends Api>> onResponse) {
        this.onResponse = onResponse;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public StreamOptions setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public StreamOptions setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public StreamOptions setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public Integer getMaxRetryDelayMs() {
        return maxRetryDelayMs;
    }

    public StreamOptions setMaxRetryDelayMs(Integer maxRetryDelayMs) {
        this.maxRetryDelayMs = maxRetryDelayMs;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public StreamOptions setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}
