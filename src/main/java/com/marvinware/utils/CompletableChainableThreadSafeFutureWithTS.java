package com.marvinware.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * The type Completable future with ts.
 *
 * @param <T> the type parameter
 */
public class CompletableChainableThreadSafeFutureWithTS<T> extends CompletableFuture<T> {
    private volatile long startTS = 0L;
    private volatile long completeTS = 0L;
    private CompletableFuture<T> chainedFuture;

    /**
     * Instantiates a new Completable future with ts.
     *
     * @param chainedFuture the chained future
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public CompletableChainableThreadSafeFutureWithTS(CompletableChainableThreadSafeFutureWithTS<T> chainedFuture) {
        this.chainedFuture = chainedFuture;
    }

    /**
     * Sets start ts.
     *
     * @param ts the ts
     */
    public synchronized void setStartTS(long ts) {
        this.startTS = ts;
    }

    /**
     * Sets complete ts.
     *
     * @param ts the ts
     */
    public synchronized void setCompleteTS(long ts) {
        this.completeTS = ts;
    }

    /**
     * Gets complete ts.
     *
     * @return the complete ts
     */
    public synchronized long getCompleteTS() {
        return completeTS;
    }

    /**
     * Gets start ts.
     *
     * @return the start ts
     */
    public synchronized long getStartTS() {
        return startTS;
    }

    /**
     * Is not started boolean.
     *
     * @return the boolean
     */
    @SuppressWarnings("unused")
    public synchronized boolean isNotStarted() {
        return completeTS == 0L && startTS == 0L;
    }

    /**
     * Supplier fetch time long.
     *
     * @return the long
     */
    public synchronized long supplierFetchTime() {
        return completeTS <= 0 ? -1 : completeTS - startTS;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T ret = super.get();

        if (getCompleteTS() <= 0) {
            setCompleteTS(System.currentTimeMillis());
        }
        return ret;
    }

    @Override
    public synchronized boolean complete(T value) {
        boolean ret = super.complete(value);
        if (getCompleteTS() <= 0) {
            setCompleteTS(System.currentTimeMillis());
        }
        if (chainedFuture != null) {
            chainedFuture.complete(value);
            chainedFuture = null;
        }

        return ret;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        boolean ret = super.cancel(mayInterruptIfRunning);
        if (chainedFuture != null) {
            chainedFuture.cancel(mayInterruptIfRunning);
            chainedFuture = null;
        }
        if (getCompleteTS() <= 0) {
            setCompleteTS(System.currentTimeMillis());
        }
        return ret;
    }



}
