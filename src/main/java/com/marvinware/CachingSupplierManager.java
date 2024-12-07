package com.marvinware;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class CachingSupplierManager<T> {

    private final CachingSupplier.SupplierConfig currentConfig;
    private final ConcurrentMap<String, CachingSupplier<T>> cachingSuppliersByResourceId = new ConcurrentHashMap<>();

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
        if (config.isCacheCleanupThreadEnabled()) {
            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(config.pollingPeriodForCleanupThread());
                        for (CachingSupplier<T> cacheSupplier : cachingSuppliersByResourceId.values()) {
                            if (cacheSupplier != null) {
                                cacheSupplier.clearCacheIfStale();
                            }
                        }
                    } catch (Exception ignored) { }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    public void registerSupplier(String resourceId, Supplier<T> supplier) {
        registerSupplier(resourceId, defaultConfig, supplier);
    }

    public void registerSupplier(String supplierId, CachingSupplier.SupplierConfig supplierConfig, Supplier<T> supplier) {
        CachingSupplier<T> newSS = new CachingSupplier<T>(supplierId, supplierConfig, supplier);
        CachingSupplier<T> oldSS = cachingSuppliersByResourceId.putIfAbsent(supplierId, newSS);
        if (oldSS != null) {
            throw new RuntimeException("A registered CachingSupplier already exists for id: " + supplierId);
        }
    }

    public String getStatsJson(String resourceId) {
        CachingSupplier<T> supplier = cachingSuppliersByResourceId.get(resourceId);
        return supplier.getJsonStats();
    }

    public T get(String resourceId) {
        CachingSupplier<T> cachingSupplier = cachingSuppliersByResourceId.get(resourceId);
        if (cachingSupplier == null) {
            throw new RuntimeException("CachingSupplier resource id not registered: " + resourceId);
        }
        return cachingSupplier.get();

    }

    protected void clear() {
        cachingSuppliersByResourceId.clear();
    }

}


