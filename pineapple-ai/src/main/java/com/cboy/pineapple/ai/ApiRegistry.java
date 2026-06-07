package com.cboy.pineapple.ai;

import com.cboy.pineapple.ai.types.Context;
import com.cboy.pineapple.ai.types.StreamOptions;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;
import com.cboy.pineapple.ai.utils.AssistantMessageEventStream;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApiRegistry {

    @FunctionalInterface
    public interface ApiStreamFunction {
        AssistantMessageEventStream apply(Model<Api> model, Context context, StreamOptions options);
    }

    @FunctionalInterface
    public interface StreamFunction<T extends Api, O extends StreamOptions> {
        AssistantMessageEventStream apply(Model<T> model, Context context, O options);
    }

    @FunctionalInterface
    public interface SimpleStreamFunction<T extends Api> {
        AssistantMessageEventStream apply(Model<T> model, Context context, StreamOptions options);
    }

    public record ApiProvider<T extends Api, O extends StreamOptions>(
            T api,
            StreamFunction<T, O> stream,
            SimpleStreamFunction<T> streamSimple
    ) {}

    public record ApiProviderInternal(
            Api api,
            ApiStreamFunction stream,
            ApiStreamFunction streamSimple
    ) {}

    private record RegisteredApiProvider(
            ApiProviderInternal provider,
            String sourceId
    ) {}

    private static final Map<Api, RegisteredApiProvider> registry = new ConcurrentHashMap<>();

    private static <T extends Api, O extends StreamOptions> ApiStreamFunction wrapStream(
            T api, StreamFunction<T, O> stream) {
        return (model, context, options) -> {
            if (!model.getApi().equals(api)) {
                throw new IllegalArgumentException(
                        "Mismatched api: " + model.getApi() + " expected " + api);
            }
            @SuppressWarnings("unchecked")
            Model<T> typedModel = (Model<T>) model;
            @SuppressWarnings("unchecked")
            O typedOptions = (O) options;
            return stream.apply(typedModel, context, typedOptions);
        };
    }

    private static <T extends Api> ApiStreamFunction wrapStreamSimple(
            T api, SimpleStreamFunction<T> streamSimple) {
        return (model, context, options) -> {
            if (!model.getApi().equals(api)) {
                throw new IllegalArgumentException(
                        "Mismatched api: " + model.getApi() + " expected " + api);
            }
            @SuppressWarnings("unchecked")
            Model<T> typedModel = (Model<T>) model;
            return streamSimple.apply(typedModel, context, options);
        };
    }

    public static <T extends Api, O extends StreamOptions> void registerApiProvider(
            ApiProvider<T, O> provider, String sourceId) {
        registry.put(provider.api(), new RegisteredApiProvider(
                new ApiProviderInternal(
                        provider.api(),
                        wrapStream(provider.api(), provider.stream()),
                        wrapStreamSimple(provider.api(), provider.streamSimple())
                ),
                sourceId
        ));
    }

    public static <T extends Api, O extends StreamOptions> void registerApiProvider(
            ApiProvider<T, O> provider) {
        registerApiProvider(provider, null);
    }

    public static Optional<ApiProviderInternal> getApiProvider(Api api) {
        RegisteredApiProvider entry = registry.get(api);
        return Optional.ofNullable(entry != null ? entry.provider() : null);
    }

    public static List<ApiProviderInternal> getApiProviders() {
        return registry.values().stream()
                .map(RegisteredApiProvider::provider)
                .toList();
    }

    public static void unregisterApiProviders(String sourceId) {
        registry.entrySet().removeIf(entry -> Objects.equals(entry.getValue().sourceId(), sourceId));
    }

    public static void clearApiProviders() {
        registry.clear();
    }
}
