package com.cboy.pineapple.ai.types.model;

public class Model <T extends Api>{
    private String id;
    private String name;
    private T api;
    private Provider provider;
    private String baseUrl;
    private boolean reasoning;

}
