package com.cboy.pineapple.ai.types.content;

public enum ContentTypeEnums {

    IMAGE("image"),
    TEXT("text"),
    THINKING("thinking"),
    TOOL_CALL("toolCall")
    ;


    private final String type;

    ContentTypeEnums(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
