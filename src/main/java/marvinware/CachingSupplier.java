package marvinware;

import marvinware.utils.CompletableFutureWithTS;

import java.util.concurrent.*;
import java.util.function.Supplier;


public class CachingSupplier<T> implements Supplier<T> {

    public interface SupplierConfig {
        long getCachedResultsTTL();

        int getMaxRunningSuppliers();

        default boolean isCachingEnabled() {
            return getCachedResultsTTL() > -1;
        }
    }

    static SupplierConfig defaultConfig = new SupplierConfig() {
        @Override
        public long getCachedResultsTTL() {
            return 2000;
        }

        @Override
        public int getMaxRunningSuppliers() {
            return 2;
        }
    };

    public enum SupplierState {
        init,
        fetching,
        cached
    }

    private static class Stats {
        private static final long LIMIT = Long.MAX_VALUE - 100000;

        private final String supplierId;
        private long suppliedFromCache = 0;
        private long suppliedFromCachingSupplier = 0;
        private long suppliedFromSupplier = 0;
        private long maxSupplierTime = 0;
        private long maxResultAge = 0;
        private long minSupplierTime = Long.MAX_VALUE;
        private long totalRunTime = 0;
        private long totalCnt = 0;

        public Stats(String supplierId) {
            this.supplierId = supplierId;
        }

        public synchronized void incrementSuppliedFromCache() {
            handleRollover();
            suppliedFromCache++;
        }

        public synchronized void incrementSuppliedFromCachingSupplier() {
            handleRollover();
            suppliedFromCachingSupplier++;
        }

        public synchronized void incrementSuppliedFromSupplier() {
            handleRollover();
            suppliedFromSupplier++;
        }

        public synchronized void handleRollover() {
            if (
                    suppliedFromCache > LIMIT ||
                            suppliedFromCachingSupplier > LIMIT ||
                            suppliedFromSupplier > LIMIT ||
                            totalRunTime > LIMIT ||
                            totalCnt > LIMIT
            ) {
                suppliedFromCache = 0;
                suppliedFromCachingSupplier = 0;
                suppliedFromSupplier = 0;
                maxSupplierTime = 0;
                minSupplierTime = Long.MAX_VALUE;
                maxResultAge = 0;
                totalRunTime = 0;
                totalCnt = 0;
            }
        }

        public synchronized String getJsonStats() {
            return "{\"supplierId\":\"" + supplierId + "\",\"count\":" + totalCnt + ",\"servedByCache\":" + suppliedFromCache + ",\"servedByCachingSupplier\":" + suppliedFromCachingSupplier + ",\"servedBySupplier\":" +
                    suppliedFromSupplier + ",\"maxSupplierTime\":" + maxSupplierTime + ",\"minSupplierTime\":" + minSupplierTime + ",\"maxResultAge\":" + maxResultAge + ",\"avgRunTime\":" + (totalRunTime / totalCnt) + "}";
        }

        private synchronized void updateStats(long fetchTime, long ageOfResult) {
            handleRollover();
            maxSupplierTime = maxSupplierTime > fetchTime ? maxSupplierTime : fetchTime;
            minSupplierTime = minSupplierTime < fetchTime ? minSupplierTime : fetchTime;
            maxResultAge = maxResultAge > ageOfResult ? maxResultAge : ageOfResult;
            totalCnt++;
            totalRunTime += fetchTime;
        }

    }

    private final String supplierId;
    private final SupplierConfig config;
    private final Supplier<T> supplier;
    private int supplierRunCount = 0;
    private SupplierState state = SupplierState.init;
    private CompletableFutureWithTS<T> lastFuture;
    private final Stats stats;


    public CachingSupplier(Supplier<T> supplier) {
        this("UNKNOWN", defaultConfig, supplier);
    }

    public CachingSupplier(String supplierId, SupplierConfig config, Supplier<T> supplier) {
        this.supplierId = supplierId;
        this.config = config;
        this.supplier = supplier;
        this.stats = new Stats(supplierId);
    }

    public String getSupplierId() {
        return supplierId;
    }

    @Override
    public T get() {
        CompletableFutureWithTS<T> localFuture;
        boolean fetchNew = false;

        synchronized (this) {
            localFuture = lastFuture;
            if (
                    state == SupplierState.init ||
                            (state == SupplierState.fetching && supplierRunCount < config.getMaxRunningSuppliers()) ||
                            (state == SupplierState.cached && isCacheStale(localFuture))
            ) {
                fetchNew = true;
                supplierRunCount++;
                state = SupplierState.fetching;
                localFuture = lastFuture = new CompletableFutureWithTS<>();
                localFuture.setStartTS(System.currentTimeMillis());
            } else if (state == SupplierState.fetching) {
                stats.incrementSuppliedFromCachingSupplier();
                localFuture = lastFuture;
            } else if (state == SupplierState.cached) {
                stats.incrementSuppliedFromCache();
                localFuture = lastFuture;
            }
        } // end of synchronized

        T result;
        if (fetchNew) {
            result = supplier.get();
            synchronized (this) {
                supplierRunCount--;
                localFuture.complete(result);
                stats.incrementSuppliedFromSupplier();
                if (config.isCachingEnabled()) {
                    state = SupplierState.cached;
                } else {
                    state = SupplierState.init;
                }
            }
        } else {
            try {
                result = localFuture.get();
            } catch (ExecutionException | NullPointerException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        stats.updateStats(localFuture.supplierFetchTime(), getAgeOfResult(localFuture));

        return result;
    }

    public synchronized boolean isCacheStale(CompletableFutureWithTS<T> f) {
        return !config.isCachingEnabled() || getAgeOfResult(f) > config.getCachedResultsTTL();
    }

    public synchronized long getAgeOfResult(CompletableFutureWithTS<T> f) {
        return (config.isCachingEnabled() && state == SupplierState.cached) ? System.currentTimeMillis() - f.getCompleteTS() : 0;
    }

    public synchronized String getJsonStats() {
        return stats.getJsonStats();
    }

}