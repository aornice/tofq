package xyz.aornice.tofq.utils.cache.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.cache.StartIndexCache;
import xyz.aornice.tofq.utils.impl.AbstractExtractionCache;

/**
 * Created by shen on 2017/5/14.
 */
public class FileStartIndexCache extends AbstractExtractionCache<Long> implements StartIndexCache {

    private LRUHashMap lruHashMap;

    private final static int capacity = 2<<10;
    private final static double prihibitRatio = 1;

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    public FileStartIndexCache() {
        super(capacity, (int)(capacity*prihibitRatio));
        init();
    }

    private void init(){
        lruHashMap = new LRUHashMap(CAPACITY);
    }

    @Override
    public Long getCache(Topic topic, long iThFile) {
        long hash = hashValue(topic, iThFile);
        return lruHashMap.get(hash);
    }

    @Override
    public void putCache(Topic topic, long iThFile, Long startIndex) {
        lruHashMap.set(hashValue(topic,iThFile), startIndex);
    }


    @Override
    public void clearCache() {
        lruHashMap.clear();
    }
}
