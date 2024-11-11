package com.marvinware;

import com.marvinware.utils.CompletableFutureWithTS;

import java.util.concurrent.*;
import java.util.function.Supplier;


public class CachingSupplier<T> implements Supplier<T> {

    private final String supplierId;
    private final SupplierConfig config;
    private final Supplier<T> supplier;
    private int supplierRunCount = 0;
    private SupplierState state = SupplierState.init;
    private CompletableFutureWithTS<T> lastFuture;
    private final Stats stats;


    public CachingSupplier(Supplier<T> supplier) {
        this("UNKNOWN", defaultConfig, supplier);
    }

    public CachingSupplier(String supplierId, SupplierConfig config, Supplier<T> supplier) {
        this.supplierId = supplierId;
        this.config = config;
        this.supplier = supplier;
        this.stats = new Stats(supplierId);
    }

    public String getSupplierId() {
        return supplierId;
    }

    @Override
    public T get() {
        CompletableFutureWithTS<T> localFuture;
        boolean fetchNew = false;

        synchronized (this) {
            localFuture = lastFuture;
            if (
                    state == SupplierState.init ||
                            (state == SupplierState.fetching && supplierRunCount < config.getMaxRunningSuppliers()) ||
                            (state == SupplierState.cached && isCacheStale(localFuture))
            ) {
                fetchNew = true;
                supplierRunCount++;
                state = SupplierState.fetching;
                localFuture = lastFuture = new CompletableFutureWithTS<>();
                localFuture.setStartTS(System.currentTimeMillis());
            } else if (state == SupplierState.fetching) {
                stats.incrementResultFromCachingSupplier();
                localFuture = lastFuture;
            } else if (state == SupplierState.cached) {
                stats.incrementResultFromCache();
                localFuture = lastFuture;
            }
        } // end of synchronized

        T result;
        if (fetchNew) {
            result = supplier.get();
            synchronized (this) {
                supplierRunCount--;
                localFuture.complete(result);
                stats.incrementResultFromSupplier();
                if (config.isCachingEnabled()) {
                    state = SupplierState.cached;
                } else {
                    state = SupplierState.init;
                }
            }
        } else {
            try {
                result = localFuture.get();
            } catch (ExecutionException | NullPointerException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        stats.updateStats(localFuture.supplierFetchTime(), getAgeOfResult(localFuture));

        return result;
    }

    public static void main(String[] args) {
        CachingSupplier<String> cs = new CachingSupplier<>(() -> {
            String result;

            // do a lookup of a result object
            result = null;  // result = ...

            return result;
        });
    }

    public synchronized boolean isCacheStale(CompletableFutureWithTS<T> f) {
        return !config.isCachingEnabled() || getAgeOfResult(f) > config.getCachedResultsTTL();
    }

    public synchronized long getAgeOfResult(CompletableFutureWithTS<T> f) {
        return (config.isCachingEnabled() && state == SupplierState.cached) ? System.currentTimeMillis() - f.getCompleteTS() : 0;
    }

    public synchronized String getJsonStats() {
        return stats.getJsonStats();
    }

    public interface SupplierConfig {
        long getCachedResultsTTL();

        int getMaxRunningSuppliers();

        default boolean isCachingEnabled() {
            return getCachedResultsTTL() > -1;
        }
    }

    static SupplierConfig defaultConfig = new SupplierConfig() {
        @Override
        public long getCachedResultsTTL() {
            return 2000;
        }

        @Override
        public int getMaxRunningSuppliers() {
            return 2;
        }
    };

    public enum SupplierState {
        init,
        fetching,
        cached
    }

    private static class Stats {
        private static final long LIMIT = Long.MAX_VALUE - 100000;

        private final String supplierId;
        private long resultFromCache = 0;
        private long resultFromCachingSupplier = 0;
        private long resultFromSupplier = 0;
        private long maxSupplierTime = 0;
        private long maxResultAge = 0;
        private long minSupplierTime = Long.MAX_VALUE;
        private long totalRunTime = 0;
        private long totalCnt = 0;

        public Stats(String supplierId) {
            this.supplierId = supplierId;
        }

        public synchronized void incrementResultFromCache() {
            handleRollover();
            resultFromCache++;
        }

        public synchronized void incrementResultFromCachingSupplier() {
            handleRollover();
            resultFromCachingSupplier++;
        }

        public synchronized void incrementResultFromSupplier() {
            handleRollover();
            resultFromSupplier++;
        }

        public synchronized void handleRollover() {
            if (
                    resultFromCache > LIMIT ||
                            resultFromCachingSupplier > LIMIT ||
                            resultFromSupplier > LIMIT ||
                            totalRunTime > LIMIT ||
                            totalCnt > LIMIT
            ) {
                resultFromCache = 0;
                resultFromCachingSupplier = 0;
                resultFromSupplier = 0;
                maxSupplierTime = 0;
                minSupplierTime = Long.MAX_VALUE;
                maxResultAge = 0;
                totalRunTime = 0;
                totalCnt = 0;
            }
        }

        public synchronized String getJsonStats() {
            return "{\"supplierId\":\"" + supplierId + "\",\"count\":" + totalCnt + ",\"servedByCache\":" + resultFromCache + ",\"servedByCachingSupplier\":" + resultFromCachingSupplier + ",\"servedBySupplier\":" +
                    resultFromSupplier + ",\"maxSupplierTime\":" + maxSupplierTime + ",\"minSupplierTime\":" + minSupplierTime + ",\"maxResultAge\":" + maxResultAge + ",\"avgRunTime\":" + (totalRunTime / totalCnt) + "}";
        }

        private synchronized void updateStats(long fetchTime, long ageOfResult) {
            handleRollover();
            maxSupplierTime = maxSupplierTime > fetchTime ? maxSupplierTime : fetchTime;
            minSupplierTime = minSupplierTime < fetchTime ? minSupplierTime : fetchTime;
            maxResultAge = maxResultAge > ageOfResult ? maxResultAge : ageOfResult;
            totalCnt++;
            totalRunTime += fetchTime;
        }

    }

}