package com.cboy.pineapple.ai.types;

public enum CacheRetention {

    SHORT("short"),
    LONG("long"),
    ;

    private final String value;

    CacheRetention(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
