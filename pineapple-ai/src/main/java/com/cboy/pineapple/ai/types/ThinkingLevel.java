package com.cboy.pineapple.ai.types;

public enum ThinkingLevel {

    OFF("off"),
    MINIMAL("minimal"),
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    XHIGH("xhigh"),
    ;

    private final String level;

    ThinkingLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
