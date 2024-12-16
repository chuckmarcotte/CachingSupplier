package com.marvinware;

import java.util.HashMap;
import java.util.Map;

/**
 * The interface Supplier config.
 */
public interface CachingSupplierConfig {
    String KEYS_PREFIX = "CachingSupplierConfig.";

    /**
     * Gets cached results ttl.
     *
     * @return the cached results ttl
     */
    default long getCachedResultsTTL() {
        return 100;
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
        return true;
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


    class ConfigProperties implements CachingSupplierConfig {
        private final Map<Object, Object> properties;
        private final String prefix;

        public static final String CachedResultsTTL = KEYS_PREFIX + "CachedResultsTTL";
        public static final String MaxConcurrentRunningSuppliers = KEYS_PREFIX + "MaxConcurrentRunningSuppliers";
        public static final String NewSupplierStaggerDelay = KEYS_PREFIX + "NewSupplierStaggerDelay";
        public static final String CacheCleanupThreadEnabled = KEYS_PREFIX + "CacheCleanupThreadEnabled";
        public static final String PollingPeriodForCleanupThread = KEYS_PREFIX + "PollingPeriodForCleanupThread";

        public ConfigProperties(String prefix, @SuppressWarnings("rawtypes") Map properties) {
            this.prefix = prefix;
            this.properties = new HashMap<Object, Object>(properties);
        }

        private String formatConfigKey(String key) {
            return (prefix != null && !prefix.trim().isEmpty() ?
                    (prefix.endsWith(".") ? prefix : prefix + ".") :
                    "") + key;
        }

        @Override
        public long getCachedResultsTTL() {
            return Long.parseLong(properties.get(formatConfigKey(CachedResultsTTL)).toString());
        }

        @Override
        public int getMaxConcurrentRunningSuppliers() {
            return Integer.parseInt(properties.get(formatConfigKey(MaxConcurrentRunningSuppliers)).toString());
        }

        @Override
        public long getNewSupplierStaggerDelay() {
            return Long.parseLong(properties.get(formatConfigKey(NewSupplierStaggerDelay)).toString());
        }

        @Override
        public boolean isCacheCleanupThreadEnabled() {
            return Boolean.parseBoolean(properties.get(formatConfigKey(CacheCleanupThreadEnabled)).toString());
        }

        @Override
        public long pollingPeriodForCleanupThread() {
            return Long.parseLong(properties.get(formatConfigKey(PollingPeriodForCleanupThread)).toString());
        }

    }
}

