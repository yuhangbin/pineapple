package com.cboy.pineapple.ai.types.transport;

public record Transport(String value) {
    public static final Transport SSE = new Transport("sse");
    public static final Transport WEBSOCKET = new Transport("websocket");
}
