package org.code13k.heets.business;

import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;
import org.apache.commons.lang3.StringUtils;
import org.code13k.heets.app.Cluster;
import org.code13k.heets.model.CacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClusteredCache {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ClusteredCache.class);

    // Const
    private static final String NAME = "Code13k-Heets-Cache-Data";

    // Data
    private IMap<String, CacheData> mData = null;

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ClusteredCache INSTANCE = new ClusteredCache();
    }

    public static ClusteredCache getInstance() {
        return ClusteredCache.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    public ClusteredCache() {
        mLogger.trace("ClusteredCache");
    }

    /**
     * Initialize
     */
    synchronized public void init() {
        if (mData == null) {
            mData = Cluster.getInstance().getHazelcastInstance().getMap(NAME);
            mData.addEntryListener(new MapEvictedListener() {
                @Override
                public void mapEvicted(MapEvent event) {
                    mLogger.debug("mapEvicted # " + event);
                }
            }, true);
            mData.addEntryListener(new MapClearedListener() {
                @Override
                public void mapCleared(MapEvent event) {
                    mLogger.debug("mapCleared # " + event);
                }
            }, true);
        } else {
            mLogger.info("Duplicated initializing");
        }
    }

    /**
     * Set
     */
    public void set(String key, CacheData value, Consumer<Boolean> consumer) {
        if (StringUtils.isEmpty(key) == false) {
            ICompletableFuture<CacheData> future = mData.putAsync(key, value, value.getExpires(), TimeUnit.SECONDS);
            future.andThen(new ExecutionCallback<CacheData>() {
                @Override
                public void onResponse(CacheData response) {
                    mLogger.trace("response = " + response);
                    if (consumer != null) {
                        consumer.accept(true);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mLogger.error("Error occurred", t);
                    if (consumer != null) {
                        consumer.accept(false);
                    }
                }
            });
        } else {
            if (consumer != null) {
                consumer.accept(false);
            }
        }
    }

    /**
     * Get
     */
    public void get(String key, Consumer<CacheData> consumer) {
        if (StringUtils.isEmpty(key) == false) {
            ICompletableFuture<CacheData> future = mData.getAsync(key);
            future.andThen(new ExecutionCallback<CacheData>() {
                @Override
                public void onResponse(CacheData response) {
                    mLogger.trace("response = " + response);
                    if (consumer != null) {
                        consumer.accept(response);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mLogger.error("Error occurred", t);
                    if (consumer != null) {
                        consumer.accept(null);
                    }
                }
            });
        } else {
            if (consumer != null) {
                consumer.accept(null);
            }
        }
    }

    /**
     * Delete
     */
    public void del(String key, Consumer<Boolean> consumer) {
        if (StringUtils.isEmpty(key) == false) {
            ICompletableFuture<CacheData> future = mData.removeAsync(key);
            future.andThen(new ExecutionCallback<CacheData>() {
                @Override
                public void onResponse(CacheData response) {
                    mLogger.trace("response = " + response);
                    if (consumer != null) {
                        consumer.accept(true);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mLogger.error("Error occurred", t);
                    if (consumer != null) {
                        consumer.accept(false);
                    }
                }
            });
        } else {
            if (consumer != null) {
                consumer.accept(false);
            }
        }
    }
}
