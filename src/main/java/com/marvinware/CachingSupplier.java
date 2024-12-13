package com.marvinware;

import com.marvinware.utils.CompletableFutureWithTS;

import java.util.concurrent.*;
import java.util.function.Supplier;


public class CachingSupplier<T> implements Supplier<T> {

    private static final System.Logger logger = System.getLogger(CachingSupplier.class.getName());

    private final String supplierId;
    private final SupplierConfig config;
    private final Supplier<T> supplier;
    private volatile int supplierRunCount = 0;
    private long previousFutureStartTime = 0L;
    private SupplierState state = SupplierState.init;
    private CompletableFutureWithTS<T> sharedFuture;
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
        T supplierResult;
        int localSupplierRunCount = supplierRunCount;

        boolean fetchNew = processCurrentState(System.currentTimeMillis());

        if (fetchNew) {
            sharedFuture.setStartTS(System.currentTimeMillis());
            localSupplierRunCount = supplierRunCount;
            supplierResult = supplier.get();
            updateState(supplierResult);
        } else {
            try {
                supplierResult = sharedFuture.get();
            } catch (ExecutionException | NullPointerException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        stats.updateStats(sharedFuture.supplierFetchTime(), getAgeOfResult(sharedFuture), localSupplierRunCount);

        return supplierResult;
    }

    private synchronized boolean processCurrentState(long now) {
        boolean newFuture = false;

        switch (state) {
            case init:
                newFuture = true;
                break;

            case fetching:
                if (notAtMaxSupplierCount() && notInSupplierStaggerDelay()) {
                    newFuture = true;
                } else {
                    stats.incrementResultFromCachingSupplier();
                }
                break;

            case cached:
                if (isCacheStale(sharedFuture) && notAtMaxSupplierCount() && notInSupplierStaggerDelay()) {
                    newFuture = true;
                } else {
                    stats.incrementResultFromCache();
                }
                break;
        }
        if (newFuture) {
            previousFutureStartTime = (sharedFuture == null) ? 0L : sharedFuture.getStartTS();
            sharedFuture = getNewCompletableFuture(sharedFuture != null && !sharedFuture.isDone() ? sharedFuture : null);
            return true;
        }
        return false;
    }

    private synchronized void updateState(T supplierResult) {
        stats.incrementResultFromSupplier();
        state = (config.isCachingEnabled()) ? SupplierState.cached : SupplierState.init;
        sharedFuture.complete(supplierResult);
        supplierRunCount--;
    }

    private synchronized CompletableFutureWithTS<T> getNewCompletableFuture(CompletableFutureWithTS<T> chainedFuture) {
        supplierRunCount++;
        state = SupplierState.fetching;
        return new CompletableFutureWithTS<>(chainedFuture);
    }

    public synchronized boolean notAtMaxSupplierCount() {
        return config.getMaxConcurrentRunningSuppliers() < 1 || supplierRunCount < config.getMaxConcurrentRunningSuppliers();
    }

    public synchronized boolean notInSupplierStaggerDelay() {
        return (System.currentTimeMillis() - previousFutureStartTime) >= config.getNewSupplierStaggerDelay();
    }

    public synchronized boolean isCacheStale(CompletableFutureWithTS<T> f) {
        return (!config.isCachingEnabled() || f == null || getAgeOfResult(f) > config.getCachedResultsTTL()) && notAtMaxSupplierCount();
    }

    public synchronized long getAgeOfResult(CompletableFutureWithTS<T> f) {
        return (config.isCachingEnabled() && state == SupplierState.cached) ? System.currentTimeMillis() - f.getCompleteTS() : 0;
    }

    public synchronized String getJsonStats() {
        return stats.getJsonStats();
    }

    public synchronized void clearCacheIfStale() {
        if (sharedFuture != null && isCacheStale(sharedFuture) && supplierRunCount == 0) {
            state = SupplierState.init;
            sharedFuture = null;
            logger.log(System.Logger.Level.WARNING, "Cleared cache for CachedSupplier with id: " + supplierId);
        }
    }

    public synchronized int getCurrentSupplierCount() {
        return supplierRunCount;
    }

    public interface SupplierConfig {
        default long getCachedResultsTTL()  { return 60000; }

        default int getMaxConcurrentRunningSuppliers()  { return 1; }

        default long getNewSupplierStaggerDelay() { return 100; }

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
            return "{\"supplierId\":\"" + supplierId + "\",\"count\":" + totalCnt + ",\"servedByCache\":" + resultFromCache +
                    ",\"servedByCachingSupplier\":" + resultFromCachingSupplier + ",\"servedBySupplier\":" + resultFromSupplier +
                    ",\"cacheHitRatio\":" + String.format("%f", (totalCnt == 0 ? 0 : (resultFromCache + resultFromCachingSupplier) / (double) totalCnt)) +
                    ",\"maxConcurrentSuppliers\":" + maxConcurrentSuppliers + ",\"maxSupplierTime\":" + maxSupplierTime + ",\"minSupplierTime\":" + minSupplierTime + ",\"maxResultAge\":" + maxResultAge + ",\"avgRunTime\":" +
                    (totalCnt == 0 ? 0 : (totalRunTime / totalCnt)) + "}";
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