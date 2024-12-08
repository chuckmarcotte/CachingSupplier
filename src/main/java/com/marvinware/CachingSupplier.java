package com.marvinware;

import com.marvinware.utils.CompletableFutureWithTS;

import java.util.concurrent.*;
import java.util.function.Supplier;


public class CachingSupplier<T> implements Supplier<T> {

    private static final System.Logger logger = System.getLogger(CachingSupplier.class.getName());

    private final String supplierId;
    private final SupplierConfig config;
    private final Supplier<T> supplier;
    private int supplierRunCount = 0;
    private long lastStart = 0L;
    private SupplierState state = SupplierState.init;
    private CompletableFutureWithTS<T> lastFuture;
    private final Stats stats;


    @SuppressWarnings("unused")
    public CachingSupplier(Supplier<T> supplier) {
        this("UNKNOWN", defaultConfig, supplier);
    }

    public CachingSupplier(String supplierId, SupplierConfig config, Supplier<T> supplier) {
        this.supplierId = supplierId;
        this.config = config;
        this.supplier = supplier;
        this.stats = new Stats(supplierId);
    }

    @SuppressWarnings("unused")
    public String getSupplierId() {
        return supplierId;
    }

    @Override
    public T get() {
        CompletableFutureWithTS<T> localFuture;
        boolean fetchNew = false;

        synchronized (this) {
            long now = System.currentTimeMillis();
            localFuture = lastFuture;

            switch(state) {
                case init:
                    fetchNew = true;
                    localFuture = getCompletableFutureWithTS(now);
                    break;

                case fetching:
                    if (supplierRunCount < config.getMaxConcurrentRunningSuppliers() &&
                            ((now - lastStart) >= config.getNewSupplierStaggerDelay())) {
                        fetchNew = true;
                        localFuture = getCompletableFutureWithTS(now);
                    } else {
                        stats.incrementResultFromCachingSupplier();
                    }
                    break;

                case cached:
                    if (isCacheStale(localFuture)) {
                        fetchNew = true;
                        localFuture = getCompletableFutureWithTS(now);
                    } else {
                        stats.incrementResultFromCache();
                    }
                    break;
            }
        } // end of synchronized

        T result;
        if (fetchNew) {
            result = supplier.get();
            synchronized (this) {
                supplierRunCount--;
                lastStart = System.currentTimeMillis();
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
        stats.updateStats(localFuture.supplierFetchTime(), getAgeOfResult(localFuture), supplierRunCount);

        return result;
    }

    private CompletableFutureWithTS<T> getCompletableFutureWithTS(long now) {
        CompletableFutureWithTS<T> localFuture;
        supplierRunCount++;
        state = SupplierState.fetching;
        localFuture = lastFuture = new CompletableFutureWithTS<>();
        localFuture.setStartTS(now);
        return localFuture;
    }

    public synchronized boolean isCacheStale(CompletableFutureWithTS<T> f) {
        return !config.isCachingEnabled() || f == null || getAgeOfResult(f) > config.getCachedResultsTTL();
    }

    public synchronized long getAgeOfResult(CompletableFutureWithTS<T> f) {
        return (config.isCachingEnabled() && state == SupplierState.cached) ? System.currentTimeMillis() - f.getCompleteTS() : 0;
    }

    public synchronized String getJsonStats() {
        return stats.getJsonStats();
    }

    public synchronized void clearCacheIfStale() {
        if (config.isCachingEnabled() && lastFuture != null && getAgeOfResult(lastFuture) > config.getCachedResultsTTL()) {
            state = SupplierState.init;
            lastFuture = null;
            logger.log(System.Logger.Level.DEBUG, "Cleared cache for CachedSupplier with id: " + supplierId);
        }
    }

    public interface SupplierConfig {
        default long getCachedResultsTTL()  { return 500; }

        default int getMaxConcurrentRunningSuppliers()  { return 5; }

        default long getNewSupplierStaggerDelay() { return 50; }

        default boolean isCacheCleanupThreadEnabled() { return false; }

        default long pollingPeriodForCleanupThread() { return 5000; }

        default boolean isCachingEnabled() { return getCachedResultsTTL() > 0; }
    }

    public static SupplierConfig defaultConfig = new SupplierConfig() { };

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
        private long maxConcurrentSuppliers = 0;
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
                            maxConcurrentSuppliers > LIMIT ||
                            totalCnt > LIMIT
            ) {
                resultFromCache = 0;
                resultFromCachingSupplier = 0;
                resultFromSupplier = 0;
                maxConcurrentSuppliers = 0;
                maxSupplierTime = 0;
                minSupplierTime = Long.MAX_VALUE;
                maxResultAge = 0;
                totalRunTime = 0;
                totalCnt = 0;
            }
        }

        public synchronized String getJsonStats() {
            return "{\"supplierId\":\"" + supplierId + "\",\"count\":" + totalCnt + ",\"servedByCache\":" + resultFromCache + ",\"servedByCachingSupplier\":" + resultFromCachingSupplier + ",\"servedBySupplier\":" +
                    resultFromSupplier + ",\"maxConcurrentSuppliers\":" + maxConcurrentSuppliers + ",\"maxSupplierTime\":" + maxSupplierTime + ",\"minSupplierTime\":" + minSupplierTime + ",\"maxResultAge\":" + maxResultAge + ",\"avgRunTime\":" + (totalRunTime / totalCnt) + "}";
        }

        @SuppressWarnings("ManualMinMaxCalculation")
        private synchronized void updateStats(long fetchTime, long ageOfResult, long concurrentSupplierCount) {
            handleRollover();
            maxSupplierTime = maxSupplierTime > fetchTime ? maxSupplierTime : fetchTime;
            minSupplierTime = minSupplierTime < fetchTime ? minSupplierTime : fetchTime;
            maxResultAge = maxResultAge > ageOfResult ? maxResultAge : ageOfResult;
            maxConcurrentSuppliers = maxConcurrentSuppliers < concurrentSupplierCount ? concurrentSupplierCount : maxConcurrentSuppliers;
            totalCnt++;
            totalRunTime += fetchTime;
        }

    }

}