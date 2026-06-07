package com.cboy.pineapple.ai;

import com.cboy.pineapple.ai.types.Context;
import com.cboy.pineapple.ai.types.StreamOptions;
import com.cboy.pineapple.ai.types.message.AssistantMessage;
import com.cboy.pineapple.ai.types.model.Api;
import com.cboy.pineapple.ai.types.model.Model;
import com.cboy.pineapple.ai.utils.AssistantMessageEventStream;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Stream {

    public static ApiRegistry.ApiProviderInternal resolveApiProvider(Api api) {
        Optional<ApiRegistry.ApiProviderInternal> provider = ApiRegistry.getApiProvider(api);
        if (provider.isEmpty()) {
            throw new RuntimeException("No API provider registered for api: " + api.value());
        }
        return provider.get();
    }

    public static AssistantMessageEventStream stream(Model<Api> model, Context context, StreamOptions options) {
        ApiRegistry.ApiProviderInternal provider = resolveApiProvider(model.getApi());
        return provider.stream().apply(model, context, options);
    }

    public static CompletableFuture<AssistantMessage> complete(Model<Api> model, Context context, StreamOptions options) {
        AssistantMessageEventStream s = stream(model, context, options);
        return s.result();
    }

    public static AssistantMessageEventStream streamSimple(Model<Api> model, Context context, StreamOptions options) {
        ApiRegistry.ApiProviderInternal provider = resolveApiProvider(model.getApi());
        return provider.streamSimple().apply(model, context, options);
    }

    public static CompletableFuture<AssistantMessage> completeSimple(Model<Api> model, Context context, StreamOptions options) {
        AssistantMessageEventStream s = streamSimple(model, context, options);
        return s.result();
    }


}
