package com.marvinware;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class CachingSupplierTest {


    @Test
    public void simpleTest() {

        CachingSupplierConfig config = new CachingSupplierConfig() {
            @Override
            public long getCachedResultsTTL() {
                return 1000;
            }

            @Override
            public int getMaxConcurrentRunningSuppliers() {
                return 2;
            }
        };

        CachingSupplier<Long> cachedSupplier = new CachingSupplier<>("id1",
                config,
                () -> {
                    Random r = new Random();
                    int i = r.nextInt(20);
                    try {
                        Thread.sleep((i + 10) * 100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return System.currentTimeMillis();
                });

        Long ret = cachedSupplier.get();


    }

}
