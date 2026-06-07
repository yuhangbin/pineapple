package com.cboy.pineapple.ai.types.tool;

/**
 * A tool definition that an LLM can invoke via function calling.
 */
public interface Tool {

    String name();

    String description();

    String parameters();

    static Tool of(String name, String description, String parameters) {
        record SimpleTool(String name, String description, String parameters) implements Tool {}
        return new SimpleTool(name, description, parameters);
    }

}
