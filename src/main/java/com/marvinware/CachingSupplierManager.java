package com.marvinware;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class CachingSupplierManager<T> {

    private final ConcurrentMap<String, CachingSupplier<T>> cachingSuppliersByResourceId = new ConcurrentHashMap<>();

    private CachingSupplier.SupplierConfig defaultConfig = new CachingSupplier.SupplierConfig() {
        @Override
        public long getCachedResultsTTL() {
            return 2000; // 2 sec cache
        }

        @Override
        public int getMaxRunningSuppliers() {
            return 2;
        }
    };

    public CachingSupplierManager() {
    }

    public CachingSupplierManager(CachingSupplier.SupplierConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Supplier<T> registerSupplier(String resourceId, Supplier<T> supplier) {
        return registerSupplier(resourceId, supplier, defaultConfig);
    }

    public Supplier<T> registerSupplier(String supplierId, Supplier<T> supplier, CachingSupplier.SupplierConfig supplierConfig) {
        CachingSupplier<T> newSS = new CachingSupplier<T>(supplierId, supplierConfig, supplier);
        CachingSupplier<T> oldSS = cachingSuppliersByResourceId.putIfAbsent(supplierId, newSS);
        return (oldSS == null) ? newSS : oldSS;
    }

    public String getStatsJson(String resourceId) {
        CachingSupplier<T> supplier = cachingSuppliersByResourceId.get(resourceId);
        return supplier.getJsonStats();
    }

    public T get(String resourceId) {
        CachingSupplier<T> cachingSupplier = cachingSuppliersByResourceId.get(resourceId);
        if (cachingSupplier == null) {
            throw new RuntimeException("CachingSupplier not registered before first usage: " + resourceId);
        }
        return cachingSupplier.get();

    }


    //////////////////////////////////////////////////////////////////////////////////////////

    static final int threadCount = 20000;

    static class TestThreadRunnable implements Runnable {

        CachingSupplierManager<Long> manager;

        public TestThreadRunnable(CachingSupplierManager<Long> manager) {
            this.manager = manager;
        }

        @Override
        public void run() {

            for (int i=0; i < 3; i++) {
                Random r = new Random();
                int num= r.nextInt(5);
                try {
                    Thread.sleep(num * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Long result = manager.get("cachingsupplier1");
            }

        }

    }


    public static void main(String[] args) {

        Supplier<Long> supImpl = () -> {
            Random r = new Random();
            int i = r.nextInt(20);
            try {
                Thread.sleep((i + 10) * 100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return System.currentTimeMillis();
        };

        CachingSupplier.SupplierConfig config = new CachingSupplier.SupplierConfig() {
            @Override
            public long getCachedResultsTTL() {
                return 1000;
            }

            @Override
            public int getMaxRunningSuppliers() {
                return 2;
            }
        };

        CachingSupplierManager<Long> manager = new CachingSupplierManager<>(config);

        manager.registerSupplier("cachingsupplier1", supImpl);

        TestThreadRunnable lr = new TestThreadRunnable(manager);

        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(lr);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        System.out.println("\n" + threads.length + " threads started!");

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("\n" + manager.getStatsJson("cachingsupplier1"));

    }
}


