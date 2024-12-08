package com.marvinware;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class CachingSupplierManager<T> {

    private final CachingSupplier.SupplierConfig currentConfig;
    private final ConcurrentMap<String, CachingSupplier<T>> cachingSuppliersByResourceId = new ConcurrentHashMap<>();
    private final System.Logger logger = System.getLogger(CachingSupplierManager.class.getName());


    public static CachingSupplier.SupplierConfig defaultConfig = new CachingSupplier.SupplierConfig() {
        @Override
        public boolean isCacheCleanupThreadEnabled() { return true; }
    };


    public CachingSupplierManager() {
        this(defaultConfig);
    }

    public CachingSupplierManager(CachingSupplier.SupplierConfig config)
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

    public void registerSupplier(String resourceId, Supplier<T> supplier) {
        registerSupplier(resourceId, defaultConfig, supplier);
    }

    public void registerSupplier(String supplierId, CachingSupplier.SupplierConfig supplierConfig, Supplier<T> supplier) {
        CachingSupplier<T> newSS = new CachingSupplier<>(supplierId, supplierConfig, supplier);
        CachingSupplier<T> oldSS = cachingSuppliersByResourceId.putIfAbsent(supplierId, newSS);
        if (oldSS != null) {
            String errorMsg = "A registered CachingSupplier already exists for id: " + supplierId;
            logger.log(System.Logger.Level.ERROR, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    public String getStatsJson(String resourceId) {
        CachingSupplier<T> supplier = cachingSuppliersByResourceId.get(resourceId);
        return supplier.getJsonStats();
    }

    public T get(String resourceId) {
        CachingSupplier<T> cachingSupplier = cachingSuppliersByResourceId.get(resourceId);
        if (cachingSupplier == null) {
            String errorMsg = "CachingSupplier resource id not registered: " + resourceId;
            logger.log(System.Logger.Level.ERROR, errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return cachingSupplier.get();

    }

    @SuppressWarnings("unused")
    protected void clear() {    // used by unit tests
        cachingSuppliersByResourceId.clear();
    }

}


