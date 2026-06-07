package com.cboy.pineapple.ai.types.model;

public class Model <T extends Api>{
    private String id;
    private String name;
    private T api;
    private Provider provider;
    private String baseUrl;
    private boolean reasoning;


    public String getId() {
        return id;
    }

    public Model<T> setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Model<T> setName(String name) {
        this.name = name;
        return this;
    }

    public T getApi() {
        return api;
    }

    public Model<T> setApi(T api) {
        this.api = api;
        return this;
    }

    public Provider getProvider() {
        return provider;
    }

    public Model<T> setProvider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Model<T> setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public boolean isReasoning() {
        return reasoning;
    }

    public Model<T> setReasoning(boolean reasoning) {
        this.reasoning = reasoning;
        return this;
    }
}
