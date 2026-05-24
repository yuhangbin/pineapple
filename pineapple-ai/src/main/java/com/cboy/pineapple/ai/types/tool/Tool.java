package com.cboy.pineapple.ai.types.tool;

import java.util.Objects;

/**
 * A tool definition that an LLM can invoke via function calling.
 *
 * Use {@link #of(String, String, String)} to create a Tool with a raw JSON
 * Schema string for {@code parameters}, matching the LLM provider's expected
 * format.
 */
public record Tool(String name, String description, String parametersJsonSchema) {

    public Tool {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(parametersJsonSchema, "parametersJsonSchema must not be null");
    }

    public static Tool of(String name, String description, String parametersJsonSchema) {
        return new Tool(name, description, parametersJsonSchema);
    }
}
