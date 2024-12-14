package com.marvinware;

import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CachingSupplierManagerTest {

    static final int threadCount = 20000;
    static final System.Logger logger = System.getLogger(CachingSupplierManagerTest.class.getName());

    @Test
    public void loadTest() {

        CachingSupplierManager<Long> manager = new CachingSupplierManager<>();

        manager.registerSupplier("supplier1", () -> {
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

        // Simulate a lot of requests - each thread could be a thread from a worker pool and does 3 requests
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
                    Long res = manager.get("supplier1");
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
                int last = manager.getCurrentSupplierCount("supplier1");
                int curr = last;
                while (thread.isAlive()) {
                    curr = manager.getCurrentSupplierCount("supplier1");
                    if (last != curr) {
                        last = curr;
//                        logger.log(System.Logger.Level.INFO, curr);
                    }
                    thread.join(5);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //logger.log(System.Logger.Level.INFO, manager.getCurrentSupplierCount("supplier1"));

        logger.log(System.Logger.Level.INFO, manager.getStatsJson("supplier1"));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }

//        logger.log(System.Logger.Level.INFO, manager.getCurrentSupplierCount("supplier1"));

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
                    Long res = manager.get("supplier1");
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
                int last = manager.getCurrentSupplierCount("supplier1");
                int curr = last;
                while (thread.isAlive()) {
                    curr = manager.getCurrentSupplierCount("supplier1");
                    if (last != curr) {
                        last = curr;
//                        logger.log(System.Logger.Level.INFO, curr);
                    }
                    thread.join(5);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        logger.log(System.Logger.Level.INFO, manager.getCurrentSupplierCount("supplier1"));

        logger.log(System.Logger.Level.INFO, manager.getStatsJson("supplier1"));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }

//        logger.log(System.Logger.Level.INFO, manager.getCurrentSupplierCount("supplier1"));

    }

}
