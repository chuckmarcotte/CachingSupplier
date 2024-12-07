package com.marvinware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Supplier;

public class CachingSupplierTest {


    @Test
    public void simpleTest() {

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

        CachingSupplier<Long> cachedSuplier = new CachingSupplier<>("id1",
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

        Long ret = cachedSuplier.get();


    }

}
