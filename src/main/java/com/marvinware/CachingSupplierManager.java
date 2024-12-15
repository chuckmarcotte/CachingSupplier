package com.marvinware;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

/**
 * The type Caching supplier manager.
 *
 * @param <T> the type parameter
 */
public class CachingSupplierManager<T> {

    private final CachingSupplierConfig currentConfig;
    private final ConcurrentMap<String, CachingSupplier<T>> cachingSuppliersByResourceId = new ConcurrentHashMap<>();
    private final System.Logger logger = System.getLogger(CachingSupplierManager.class.getName());


    /**
     * The constant defaultConfig.
     */
    public static final CachingSupplierConfig defaultConfig = new CachingSupplierConfig() {
        @Override
        public boolean isCacheCleanupThreadEnabled() { return true; }
    };


    /**
     * Instantiates a new Caching supplier manager.
     */
    public CachingSupplierManager() {
        this(defaultConfig);
    }

    /**
     * Instantiates a new Caching supplier manager.
     *
     * @param config the config
     */
    public CachingSupplierManager(CachingSupplierConfig config)
    {
        this.currentConfig = config;

        if (currentConfig.isCacheCleanupThreadEnabled() &&
                currentConfig.isCachingEnabled() &&
                currentConfig.pollingPeriodForCleanupThread() <= 0) {
            String errorMsg = "Cache cleanup thread configuration has an invalid polling period value: " + currentConfig.pollingPeriodForCleanupThread();
            logger.log(System.Logger.Level.ERROR, errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (config.isCacheCleanupThreadEnabled()) {
            Thread t = getCleanerThread();
            t.start();
        }
    }

    private Thread getCleanerThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    sleep(currentConfig.pollingPeriodForCleanupThread());
                    for (CachingSupplier<T> cacheSupplier : cachingSuppliersByResourceId.values()) {
                        if (cacheSupplier != null) {
                            cacheSupplier.clearCacheIfStale();
                        }
                    }
                } catch (Exception ignored) { }
            }
        });
        t.setDaemon(true);
        t.setName(this.getClass().getName() + ".CacheCleanup");
        return t;
    }

    /**
     * Register supplier.
     *
     * @param resourceId the resource id
     * @param supplier   the supplier
     */
    public void registerSupplier(String resourceId, Supplier<T> supplier) {
        registerSupplier(resourceId, currentConfig, supplier);
    }

    /**
     * Register supplier.
     *
     * @param supplierId     the supplier id
     * @param cachingSupplierConfig the supplier config
     * @param supplier       the supplier
     */
    public void registerSupplier(String supplierId, CachingSupplierConfig cachingSupplierConfig, Supplier<T> supplier) {
        CachingSupplier<T> newSS = new CachingSupplier<>(supplierId, cachingSupplierConfig, supplier);
        CachingSupplier<T> oldSS = cachingSuppliersByResourceId.putIfAbsent(supplierId, newSS);
        if (oldSS != null) {
            String errorMsg = "A registered CachingSupplier already exists for id: " + supplierId;
            logger.log(System.Logger.Level.ERROR, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * Gets stats json.
     *
     * @param resourceId the resource id
     * @return the stats json
     */
    public String getStatsJson(String resourceId) {
        CachingSupplier<T> supplier = cachingSuppliersByResourceId.get(resourceId);
        return supplier.getJsonStats();
    }

    /**
     * Gets current supplier count.
     *
     * @param resourceId the resource id
     * @return the current supplier count
     */
    public int getCurrentSupplierCount(String resourceId) {
        CachingSupplier<T> supplier = cachingSuppliersByResourceId.get(resourceId);
        return supplier.getCurrentSupplierCount();
    }

    /**
     * Get t.
     *
     * @param resourceId the resource id
     * @return the t
     */
    public T get(String resourceId) {
        CachingSupplier<T> cachingSupplier = cachingSuppliersByResourceId.get(resourceId);
        if (cachingSupplier == null) {
            String errorMsg = "CachingSupplier resource id not registered: " + resourceId;
            logger.log(System.Logger.Level.ERROR, errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return cachingSupplier.get();

    }

    /**
     * Clear.
     */
    @SuppressWarnings("unused")
    protected void clear() {    // used by unit tests
        cachingSuppliersByResourceId.clear();
    }

}


