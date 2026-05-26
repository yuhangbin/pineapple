package com.cboy.pineapple.agent.core.types;

import java.util.concurrent.atomic.AtomicBoolean;

public class AbortSignal {

    private final AtomicBoolean aborted = new AtomicBoolean(false);

    public void abort() {
        aborted.set(true);
    }

    public boolean isAborted() {
        return aborted.get();
    }

    public void throwIfAborted() {
        if (aborted.get()) {
            throw new AbortException("Operation was aborted");
        }
    }

    public static class AbortException extends RuntimeException {
        public AbortException(String message) {
            super(message);
        }
    }
}
