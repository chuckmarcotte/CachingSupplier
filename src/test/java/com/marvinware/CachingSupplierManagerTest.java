package com.marvinware;

import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Random;

import static com.marvinware.CachingSupplierConfig.*;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;


public class CachingSupplierManagerTest {

    static final int threadCount = 10;
    static final System.Logger logger = System.getLogger(CachingSupplierManagerTest.class.getName());

    @Test
    public void loadTest() {
        String configPrefix = "test1.";

        CachingSupplierConfig config = new ConfigProperties(configPrefix, Map.ofEntries(
                entry(configPrefix + ConfigProperties.CachedResultsTTL, "100"),
                entry(configPrefix + ConfigProperties.MaxConcurrentRunningSuppliers, "10"),
                entry(configPrefix + ConfigProperties.NewSupplierStaggerDelay, "100"),
                entry(configPrefix + ConfigProperties.CacheCleanupThreadEnabled, "true"),
                entry(configPrefix + ConfigProperties.PollingPeriodForCleanupThread, "10000")
        ));

        CachingSupplierManager<Long> manager = new CachingSupplierManager<>(config);

        manager.registerSupplier("test1", () -> {
            // Simple Supplier that sleeps and returns a Long
            Random r = new Random();
            int i = r.nextInt(20);
            try {
                Thread.sleep((i + 10) * 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return System.currentTimeMillis();
        });

        // Simulate a lot of requests - each thread could be a thread from a worker pool and does 3 requests to form a client response
        Thread[] threads = new Thread[threadCount];
        Long[] results = new Long[threadCount];
        for (int t = 0; t < threads.length; t++) {
            final int tIndex = t;
            threads[tIndex] = new Thread(() -> {
                Random r = new Random();
                for (int i = 0; i < 3; i++) {
                    int num = r.nextInt(100);
                    try {
                        Thread.sleep(num * 20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Long res = manager.get("test1");
                    synchronized(results) {
                        results[tIndex] = res;
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        logger.log(System.Logger.Level.INFO, threads.length + " threads started!");

        for (Thread thread : threads) {
            try {
                int last = manager.getCurrentSupplierCount("test1");
                int curr = last;
                while (thread.isAlive()) {
                    curr = manager.getCurrentSupplierCount("test1");
                    if (last != curr) {
                        last = curr;
                    }
                    thread.join(5);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        manager.logJsonStats(true);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }

        for (int j=0; j < threads.length; j++) {
            assertNotEquals(null, results[j]);
            assertTrue(results[j] <= System.currentTimeMillis());
        }

        for (int t = 0; t < threads.length; t++) {
            final int tIndex = t;
            threads[tIndex] = new Thread(() -> {
                Random r = new Random();
                for (int i = 0; i < 3; i++) {
                    int num = r.nextInt(100);
                    try {
                        Thread.sleep(num * 20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Long res = manager.get("test1");
                    synchronized(results) {
                        results[tIndex] = res;
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        logger.log(System.Logger.Level.INFO, threads.length + " threads started!");

        for (Thread thread : threads) {
            try {
                int last = manager.getCurrentSupplierCount("test1");
                int curr = last;
                while (thread.isAlive()) {
                    curr = manager.getCurrentSupplierCount("test1");
                    if (last != curr) {
                        last = curr;
                    }
                    thread.join(5);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        manager.logJsonStats(true);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }

        logger.log(System.Logger.Level.INFO, manager.getCurrentSupplierCount("test1"));

    }

}
