package com.cboy.pineapple.ai.model;

public record Provider(String value) {
    public static final Provider OPENAI = new Provider("openai");
    public static final Provider ANTHROPIC = new Provider("anthropic");
    public static final Provider GOOGLE = new Provider("google");
    public static final Provider DEEPSEEK = new Provider("deepseek");
}
