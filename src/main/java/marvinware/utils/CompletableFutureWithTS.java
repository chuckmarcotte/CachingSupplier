package marvinware.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureWithTS<T> extends CompletableFuture<T> {
    volatile long startTS = 0;
    volatile long completeTS = 0;

    public CompletableFutureWithTS() {}

    public synchronized void setStartTS(long ts) {
        this.startTS = ts;
    }

    public synchronized long getCompleteTS() {
        return completeTS;
    }

    public synchronized long supplierFetchTime() {
        return completeTS <= 0 ? -1 : completeTS - startTS;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T ret = super.get();
        if (completeTS <= 0) {
            completeTS = System.currentTimeMillis();
        }
        return ret;
    }

    @Override
    public boolean complete(T value) {
        boolean ret = super.complete(value);
        if (completeTS <= 0) {
            completeTS = System.currentTimeMillis();
        }
        return ret;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean ret = super.cancel(mayInterruptIfRunning);
        if (completeTS <= 0) {
            completeTS = System.currentTimeMillis();
        }
        return ret;
    }

}
