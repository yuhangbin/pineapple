package com.cboy.pineapple.ai.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic event stream for async iteration.
 *
 * <p>Producers call {@link #push} to deliver events and {@link #end} to signal
 * completion. Consumers iterate via the blocking {@link #iterator()} or access the
 * final result via {@link #result()}.</p>
 *
 * @param <T> event type
 * @param <R> final result type extracted from the completing event
 */
public class EventStream<T, R> implements Iterable<T> {

    private static final Object POISON = new Object();

    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private final CompletableFuture<R> finalResult = new CompletableFuture<>();
    private final Predicate<T> isComplete;
    private final Function<T, R> extractResult;
    private volatile boolean done = false;

    public EventStream(Predicate<T> isComplete, Function<T, R> extractResult) {
        this.isComplete = isComplete;
        this.extractResult = extractResult;
    }

    /**
     * Push an event into the stream.
     * If the event satisfies {@code isComplete}, the stream is marked done and
     * the final result is resolved via {@code extractResult}.
     */
    @SuppressWarnings("unchecked")
    public void push(T event) {
        if (done) return;

        if (isComplete.test(event)) {
            done = true;
            finalResult.complete(extractResult.apply(event));
        }

        queue.offer(event);

        if (done) {
            queue.offer(POISON);
        }
    }

    /** Signal end without resolving the final result. */
    public void end() {
        if (done) return;
        done = true;
        queue.offer(POISON);
    }

    /** Signal end and resolve the final result. */
    public void end(R result) {
        if (done) return;
        done = true;
        finalResult.complete(result);
        queue.offer(POISON);
    }

    /** Signal failure and unblock both result consumers and iterators. */
    public void fail(Throwable error) {
        if (done) return;
        done = true;
        finalResult.completeExceptionally(error);
        queue.offer(POISON);
    }

    /** Return a future that resolves when the stream completes. */
    public CompletableFuture<R> result() {
        return finalResult;
    }

    /**
     * Return a blocking iterator over the events.
     * Blocks on {@code hasNext()} when no events are available.
     * Terminates when the stream is ended or a completing event is pushed.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Object nextItem;
            private boolean fetched;

            @Override
            public boolean hasNext() {
                if (fetched) return true;
                try {
                    nextItem = queue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                if (nextItem == POISON) return false;
                fetched = true;
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                if (!fetched && !hasNext()) throw new NoSuchElementException();
                fetched = false;
                return (T) nextItem;
            }
        };
    }
}
