package com.cboy.pineapple.ai;

import com.cboy.pineapple.ai.types.StreamOptions;
import com.cboy.pineapple.ai.types.ThinkingBudgets;
import com.cboy.pineapple.ai.types.ThinkingLevel;

public class SimpleStreamOptions extends StreamOptions {

    private ThinkingLevel reasoning;
    private ThinkingBudgets thinkingBudgets;

    public SimpleStreamOptions() {}

    public SimpleStreamOptions(SimpleStreamOptions other) {
        super(other);
        this.reasoning = other.reasoning;
        this.thinkingBudgets = other.thinkingBudgets;
    }

    public ThinkingLevel getReasoning() {
        return reasoning;
    }

    public SimpleStreamOptions setReasoning(ThinkingLevel reasoning) {
        this.reasoning = reasoning;
        return this;
    }

    public ThinkingBudgets getThinkingBudgets() {
        return thinkingBudgets;
    }

    public SimpleStreamOptions setThinkingBudgets(ThinkingBudgets thinkingBudgets) {
        this.thinkingBudgets = thinkingBudgets;
        return this;
    }
}
