package com.marvinware;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The interface Supplier config.
 */
public interface CachingSupplierConfig {
    /**
     * Gets cached results ttl.
     *
     * @return the cached results ttl
     */
    default long getCachedResultsTTL() {
        return 0;
    }

    /**
     * Gets max concurrent running suppliers.
     *
     * @return the max concurrent running suppliers
     */
    default int getMaxConcurrentRunningSuppliers() {
        return 10;
    }

    /**
     * Gets new supplier stagger delay.
     *
     * @return the new supplier stagger delay
     */
    default long getNewSupplierStaggerDelay() {
        return 100;
    }

    /**
     * Is cache cleanup thread enabled boolean.
     *
     * @return the boolean
     */
    default boolean isCacheCleanupThreadEnabled() {
        return false;
    }

    /**
     * Polling period for cleanup thread long.
     *
     * @return the long
     */
    default long pollingPeriodForCleanupThread() {
        return 5000;
    }

    /**
     * Is caching enabled boolean.
     *
     * @return the boolean
     */
    default boolean isCachingEnabled() {
        return getCachedResultsTTL() > 0;
    }

    public class ConfigProperties implements CachingSupplierConfig {
        private final Map<Object, Object> properties;
        private final String prefix;

        public static final String CachedResultsTTL = "CachedResultsTTL";
        public static final String MaxConcurrentRunningSuppliers = "MaxConcurrentRunningSuppliers";
        public static final String NewSupplierStaggerDelay = "NewSupplierStaggerDelay";
        public static final String CacheCleanupThreadEnabled = "CacheCleanupThreadEnabled";
        public static final String PollingPeriodForCleanupThread = "PollingPeriodForCleanupThread";

        public ConfigProperties(String prefix, @SuppressWarnings("rawtypes") Map properties) {
            this.prefix = prefix;
            this.properties = new HashMap<Object, Object>(properties);
        }

        @Override
        public long getCachedResultsTTL() {
            return Long.parseLong(properties.get(prefix + ".CachingSupplierConfig." + CachedResultsTTL).toString());
        }

        @Override
        public int getMaxConcurrentRunningSuppliers() {
            return Integer.parseInt(properties.get(prefix + ".CachingSupplierConfig." + MaxConcurrentRunningSuppliers).toString());
        }

        @Override
        public long getNewSupplierStaggerDelay() {
            return Long.parseLong(properties.get(prefix + ".CachingSupplierConfig." + NewSupplierStaggerDelay).toString());
        }

        @Override
        public boolean isCacheCleanupThreadEnabled() {
            return Boolean.parseBoolean(properties.get(prefix + ".CachingSupplierConfig." + CacheCleanupThreadEnabled).toString());
        }

        @Override
        public long pollingPeriodForCleanupThread() {
            return Long.parseLong(properties.get(prefix + ".CachingSupplierConfig." + PollingPeriodForCleanupThread).toString());
        }

        @Override
        public boolean isCachingEnabled() {
            return getCachedResultsTTL() > 0;
        }
    }
}

