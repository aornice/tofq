package xyz.aornice.tofq.utils.cache;

import xyz.aornice.tofq.Topic;

/**
 * Created by shen on 2017/5/13.
 */
public interface Cache<T> {
    void clearCache();

    T getCache(Topic topic, long startIndex);

    void putCache(Topic topic, long startIndex, T cacheValue);

    int getCapacity();
}
