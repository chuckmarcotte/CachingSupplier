package com.marvinware;

import com.marvinware.utils.CompletableChainableFutureWithTS;

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
    private CompletableChainableFutureWithTS<T> sharedFuture;

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
        long delegateStartTS;
        long supplierTime = -1L;
        long futureTime = -1L;
        int localSupplierCount = 0;

        boolean fetchNew = processCurrentState();

        delegateStartTS = System.currentTimeMillis();
        try {
            if (fetchNew) {
                localSupplierCount = getCurrentSupplierCount();
                supplierResult = supplier.get();
                updateState(supplierResult);
                supplierTime = System.currentTimeMillis() - delegateStartTS;
            } else {
                localSupplierCount = getCurrentSupplierCount();
                supplierResult = sharedFuture.get();
                futureTime = System.currentTimeMillis() - delegateStartTS;
            }
        } catch (InterruptedException | ExecutionException e) {
            String errorMessage = "Error in CachingSupplier get() invocation";
            throw new RuntimeException(errorMessage, e);
        }

        stats.updateStats(supplierTime, futureTime, System.currentTimeMillis() - localStartTS, localSupplierCount);

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
            sharedFuture = new CompletableChainableFutureWithTS<>(
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
        return getResultAge() > config.getCachedResultsTTL();
    }

    /**
     * Gets age of result.
     *
     * @return the age of result
     */
    public synchronized long getResultAge() {
        return (sharedFuture != null) ? sharedFuture.getResultAge() : 0L;
    }

    /**
     * Gets json stats.
     *
     * @return the json stats
     */
    public synchronized String getJsonStats(boolean reset) {
        String ret = stats.getJsonStats();
        if (reset) {
            stats.resetStats();
        }
        return ret;
    }

    public synchronized void resetStats() {
        stats.resetStats();
    }

    /**
     * Clear cache if stale.
     */
    public synchronized void clearCacheIfStale() {
        if (sharedFuture != null && isCacheStale() && supplierRunCount == 0) {
            state = SupplierState.init;
            sharedFuture = null;
            logger.log(System.Logger.Level.INFO, "Cleared cache for CachedSupplier with id: " + supplierId);
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
        private static final long LIMIT = Long.MAX_VALUE - 100000L;

        private final String supplierId;
        private long resultsFromCache = 0L;
        private long resultsFromFuture = 0L;
        private long resultsFromSupplier = 0L;
        private long maxConcurrentSuppliers = 0L;
        private long maxSupplierTime = 0L;
        private long maxGetTime = 0L;
        private long maxFutureTime = 0L;
        private long totalGetTime = 0L;
        private long totalCnt = 0L;

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
            resultsFromCache++;
        }

        /**
         * Increment result from caching supplier.
         */
        public synchronized void incrementResultFromCachingSupplier() {
            handleRollover();
            resultsFromFuture++;
        }

        /**
         * Increment result from supplier.
         */
        public synchronized void incrementResultFromSupplier() {
            handleRollover();
            resultsFromSupplier++;
        }

        /**
         * Handle rollover.
         */
        public synchronized void handleRollover() {
            if (
                    resultsFromCache > LIMIT ||
                    resultsFromFuture > LIMIT ||
                    resultsFromSupplier > LIMIT ||
                    totalGetTime > LIMIT ||
                    maxConcurrentSuppliers > LIMIT ||
                    totalCnt > LIMIT
            ) {
                resetStats();
            }
        }

        public synchronized void resetStats() {
            resultsFromCache = 0L;
            resultsFromFuture = 0L;
            resultsFromSupplier = 0L;
            maxConcurrentSuppliers = 0L;
            maxSupplierTime = 0L;
            maxFutureTime = 0L;
            maxGetTime = 0L;
            totalGetTime = 0L;
            totalCnt = 0L;
        }

        /**
         * Gets json stats.
         *
         * @return the json stats
         */
        public synchronized String getJsonStats() {
            return "{\"supplierId\":\"" + supplierId + "\",\"count\":" + totalCnt + ",\"resultsFromCache\":" + resultsFromCache +
                    ",\"resultsFromFuture\":" + resultsFromFuture + ",\"resultsFromSupplier\":" + resultsFromSupplier +
                    ",\"cacheHitRatio\":" + String.format("%f", (totalCnt == 0 ? 0 : (resultsFromCache + resultsFromFuture) / (double) totalCnt)) +
                    ",\"maxConcurrentSuppliers\":" + maxConcurrentSuppliers + ",\"maxSupplierTime\":" + maxSupplierTime + ",\"maxFutureTime\":" + maxFutureTime + ",\"maxGetTime\":" + maxGetTime + ",\"avgGetTime\":" +
                    (totalCnt == 0 ? 0 : (totalGetTime / totalCnt)) + "}";
        }

        @SuppressWarnings("ManualMinMaxCalculation")
        private synchronized void updateStats(long supplierTime, long futureTime, long getCallTime, long concurrentSupplierCount) {
            handleRollover();
            maxSupplierTime = supplierTime == -1 || maxSupplierTime > supplierTime ? maxSupplierTime : supplierTime;
            maxFutureTime = futureTime == -1 || maxFutureTime > futureTime ? maxFutureTime : futureTime;
            maxGetTime = maxGetTime > getCallTime ? maxGetTime : getCallTime;
            maxConcurrentSuppliers = maxConcurrentSuppliers < concurrentSupplierCount ? concurrentSupplierCount : maxConcurrentSuppliers;
            totalCnt++;
            totalGetTime += getCallTime;
        }

    }

}