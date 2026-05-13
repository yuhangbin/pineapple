package com.cboy.pineapple.ai.model;

import java.util.Objects;

public record Api(String value) {
    public static final Api OPENAI_COMPLETIONS = new Api("openai-completions");
    public static final Api MISTRAL_CONVERSATIONS = new Api("mistral-conversations");
    public static final Api OPENAI_RESPONSES = new Api("openai-responses");
    public static final Api AZURE_OPENAI_RESPONSES = new Api("azure-openai-responses");
    public static final Api OPENAI_CODEX_RESPONSES = new Api("openai-codex-responses");
    public static final Api ANTHROPIC_MESSAGES = new Api("anthropic-messages");
    public static final Api BEDROCK_CONVERSE_STREAM = new Api("bedrock-converse-stream");
    public static final Api GOOGLE_GENERATIVE_AI = new Api("google-generative-ai");
    public static final Api GOOGLE_VERTEX = new Api("google-vertex");

    public Api { Objects.requireNonNull(value); }
}
