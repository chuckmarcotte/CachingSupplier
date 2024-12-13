package com.marvinware.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureWithTS<T> extends CompletableFuture<T> {
    private volatile long startTS = 0L;
    private volatile long completeTS = 0L;
    CompletableFutureWithTS<T> chainedFuture;

    public CompletableFutureWithTS(CompletableFutureWithTS<T> chainedFuture) {
        this.chainedFuture = chainedFuture;
    }

    public synchronized void setStartTS(long ts) {
        this.startTS = ts;
    }

    public synchronized void setCompleteTS(long ts) {
        this.completeTS = ts;
    }

    public synchronized long getCompleteTS() {
        return completeTS;
    }

    public synchronized long getStartTS() {
        return startTS;
    }

    public synchronized long getChainedFutureStartTS() {
        return (chainedFuture != null) ? chainedFuture.getStartTS() : 0;
    }

    public synchronized boolean isNotStarted() {
        return completeTS == 0L && startTS == 0L;
    }

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
    public boolean complete(T value) {
        boolean ret = super.complete(value);
        if (chainedFuture != null) {
            chainedFuture.complete(value);
        }

        if (getCompleteTS() <= 0) {
            setCompleteTS(System.currentTimeMillis());
        }

        return ret;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean ret = super.cancel(mayInterruptIfRunning);

        if (chainedFuture != null) {
            chainedFuture.cancel(mayInterruptIfRunning);
        }

        if (getCompleteTS() <= 0) {
            setCompleteTS(System.currentTimeMillis());
        }

        return ret;
    }

}
