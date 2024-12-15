package com.marvinware;

import com.marvinware.utils.CompletableChainableThreadSafeFutureWithTS;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;


/**
 * The type Caching supplier.
 *
 * @param <T> the type parameter
 */
public class CachingSupplier<T> implements Supplier<T> {

    /**
     * The constant defaultConfig.
     */
    public static final CachingSupplierConfig defaultConfig = new CachingSupplierConfig() { };
    private static final System.Logger logger = System.getLogger(CachingSupplier.class.getName());
    private final String supplierId;
    private final CachingSupplierConfig config;
    private final Supplier<T> supplier;
    private final Stats stats;
    private volatile int supplierRunCount = 0;
    private long previousFutureStartTime = 0L;
    private SupplierState state = SupplierState.init;
    private CompletableChainableThreadSafeFutureWithTS<T> sharedFuture;

    /**
     * Instantiates a new Caching supplier.
     *
     * @param supplier the supplier
     */
    @SuppressWarnings("unused")
    public CachingSupplier(Supplier<T> supplier) {
        this("UNKNOWN", defaultConfig, supplier);
    }

    /**
     * Instantiates a new Caching supplier.
     *
     * @param supplierId the supplier id
     * @param config     the config
     * @param supplier   the supplier
     */
    public CachingSupplier(String supplierId, CachingSupplierConfig config, Supplier<T> supplier) {
        this.supplierId = supplierId;
        this.config = config;
        this.supplier = supplier;
        this.stats = new Stats(supplierId);
    }

    /**
     * Gets supplier id.
     *
     * @return the supplier id
     */
    @SuppressWarnings("unused")
    public String getSupplierId() {
        return supplierId;
    }

    // get() is NOT synchronized
    @Override
    public T get() {
        T supplierResult;
        long localStartTS = System.currentTimeMillis();
        int localSupplierCount = 0;

        boolean fetchNew = processCurrentState();

        try {
            if (fetchNew) {
                localSupplierCount = getCurrentSupplierCount();
                supplierResult = supplier.get();
                updateState(supplierResult);
            } else {
                localSupplierCount = getCurrentSupplierCount();
                supplierResult = sharedFuture.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            String errorMessage = "Error in CachingSupplier get() invocation";
            logger.log(System.Logger.Level.ERROR, errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        stats.updateStats(sharedFuture.supplierFetchTime(), System.currentTimeMillis() - localStartTS, localSupplierCount);

        return supplierResult;
    }

    private synchronized boolean processCurrentState() {
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
                if (isCacheStale() && notAtMaxSupplierCount() && notInSupplierStaggerDelay()) {
                    newFuture = true;
                } else {
                    stats.incrementResultFromCache();
                }
                break;
        }
        if (newFuture) {
            supplierRunCount++;
            state = SupplierState.fetching;
            previousFutureStartTime = (sharedFuture == null) ? 0L : sharedFuture.getStartTS();
            sharedFuture = new CompletableChainableThreadSafeFutureWithTS<>(
                    sharedFuture != null && !sharedFuture.isDone() ? sharedFuture : null);
            sharedFuture.setStartTS(System.currentTimeMillis());
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

    /**
     * Not at max supplier count boolean.
     *
     * @return the boolean
     */
    public synchronized boolean notAtMaxSupplierCount() {
        return config.getMaxConcurrentRunningSuppliers() < 1 || supplierRunCount < config.getMaxConcurrentRunningSuppliers();
    }

    /**
     * Not in supplier stagger delay boolean.
     *
     * @return the boolean
     */
    public synchronized boolean notInSupplierStaggerDelay() {
        return (System.currentTimeMillis() - previousFutureStartTime) >= config.getNewSupplierStaggerDelay();
    }

    /**
     * Is cache stale boolean.
     *
     * @return the boolean
     */
    public synchronized boolean isCacheStale() {
        return (!config.isCachingEnabled() ||
                sharedFuture == null ||
                getAgeOfResult() > config.getCachedResultsTTL()) && notAtMaxSupplierCount();
    }

    /**
     * Gets age of result.
     *
     * @return the age of result
     */
    public synchronized long getAgeOfResult() {
        return (config.isCachingEnabled() && sharedFuture != null &&
                state == SupplierState.cached) ? System.currentTimeMillis() - sharedFuture.getCompleteTS() : 0;
    }

    /**
     * Gets json stats.
     *
     * @return the json stats
     */
    public synchronized String getJsonStats() {
        return stats.getJsonStats();
    }

    /**
     * Clear cache if stale.
     */
    public synchronized void clearCacheIfStale() {
        if (sharedFuture != null && isCacheStale() && supplierRunCount == 0) {
            state = SupplierState.init;
            sharedFuture = null;
            logger.log(System.Logger.Level.WARNING, "Cleared cache for CachedSupplier with id: " + supplierId);
        }
    }

    /**
     * Gets current supplier count.
     *
     * @return the current supplier count
     */
    public synchronized int getCurrentSupplierCount() {
        return supplierRunCount;
    }

    /**
     * The enum Supplier state.
     */
    public enum SupplierState {
        /**
         * Init supplier state.
         */
        init,
        /**
         * Fetching supplier state.
         */
        fetching,
        /**
         * Cached supplier state.
         */
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

        /**
         * Instantiates a new Stats.
         *
         * @param supplierId the supplier id
         */
        public Stats(String supplierId) {
            this.supplierId = supplierId;
        }

        /**
         * Increment result from cache.
         */
        public synchronized void incrementResultFromCache() {
            handleRollover();
            resultFromCache++;
        }

        /**
         * Increment result from caching supplier.
         */
        public synchronized void incrementResultFromCachingSupplier() {
            handleRollover();
            resultFromCachingSupplier++;
        }

        /**
         * Increment result from supplier.
         */
        public synchronized void incrementResultFromSupplier() {
            handleRollover();
            resultFromSupplier++;
        }

        /**
         * Handle rollover.
         */
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

        /**
         * Gets json stats.
         *
         * @return the json stats
         */
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