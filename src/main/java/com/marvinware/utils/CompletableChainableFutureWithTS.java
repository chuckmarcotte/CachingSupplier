package com.marvinware.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The type Completable future with ts.
 *
 * @param <T> the type parameter
 */
public class CompletableChainableFutureWithTS<T> extends CompletableFuture<T> {
    private volatile long startTS = 0L;
    private volatile long completeTS = 0L;
    private CompletableFuture<T> chainedFuture;

    /**
     * Instantiates a new Completable future with ts.
     *
     * @param chainedFuture the chained future
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public CompletableChainableFutureWithTS(CompletableChainableFutureWithTS<T> chainedFuture) {
        this.chainedFuture = chainedFuture;
    }

    /**
     * Sets start ts.
     *
     * @param ts the ts
     */
    public void setStartTS(long ts) {
        this.startTS = ts;
    }

    /**
     * Sets complete ts.
     *
     * @param ts the ts
     */
    public void setCompleteTS(long ts) {
        this.completeTS = ts;
    }

    /**
     * Gets complete ts.
     *
     * @return the complete ts
     */
    public long getCompleteTS() {
        return completeTS;
    }

    public long getResultAge() {
        return getCompleteTS() > 0L ? System.currentTimeMillis() - getCompleteTS() : 0L;
    }

    /**
     * Gets start ts.
     *
     * @return the start ts
     */
    public long getStartTS() {
        return startTS;
    }

    /**
     * Supplier fetch time long.
     *
     * @return the long
     */
    public long supplierFetchTime() {
        return completeTS <= 0L ? -1L : completeTS - startTS;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            T ret = super.get();
            if (getCompleteTS() <= 0L) {
                setCompleteTS(System.currentTimeMillis());
            }
            return ret;
        } catch (InterruptedException | ExecutionException e) {
            setCompleteTS(System.currentTimeMillis());
            throw e;
        }
    }

    /**
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            T ret = super.get(timeout, unit);
            if (getCompleteTS() <= 0L) {
                setCompleteTS(System.currentTimeMillis());
            }
            return ret;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            setCompleteTS(System.currentTimeMillis());
            throw e;
        }
    }

    @Override
    public boolean complete(T value) {
        boolean ret = super.complete(value);
        setCompleteTS(System.currentTimeMillis());
        if (chainedFuture != null) {
            chainedFuture.complete(value);
            chainedFuture = null;
        }
        return ret;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean ret = super.cancel(mayInterruptIfRunning);
        setCompleteTS(System.currentTimeMillis());
        if (chainedFuture != null) {
            chainedFuture.cancel(mayInterruptIfRunning);
            chainedFuture = null;
        }
        return ret;
    }

}
