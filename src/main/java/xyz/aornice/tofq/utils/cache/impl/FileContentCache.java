package xyz.aornice.tofq.utils.cache.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.cache.ContentCache;
import xyz.aornice.tofq.utils.cache.MessageListener;
import xyz.aornice.tofq.utils.impl.AbstractExtractionCache;

import java.util.List;

/**
 * Created by shen on 2017/5/14.
 */
public class FileContentCache extends AbstractExtractionCache<List<byte[]>> implements ContentCache, MessageListener {

    private LRUHashMap lruHashMap;

    private static final int capacity = 1024;
    private static final double prihibitRatio = 0.1;


    @Override
    public int getCapacity() {
        return CAPACITY;
    }


    public void appendMessage(Cargo cargo, long startIndex) {
        long hash = hashValue(cargo.getTopic(), startIndex);
        List<byte[]> fileContents = lruHashMap.get(hash);
        if (fileContents != null) {
            fileContents.add(cargo.getDataArray());
        }
    }


    public FileContentCache() {
        super(capacity, (int) (capacity * prihibitRatio));
        init();
    }

    private void init() {
        lruHashMap = new LRUHashMap(CAPACITY);
    }

    @Override
    public List<byte[]> getCache(Topic topic, long startIndex) {
        long hash = hashValue(topic, startIndex);
        return lruHashMap.get(hash);
    }

    @Override
    public void putCache(Topic topic, long startIndex, List<byte[]> messages) {
        long hash = hashValue(topic, startIndex);
        lruHashMap.set(hash, messages);
    }


    @Override
    public void messageAdded(Cargo cargo, long offset) {
        long startIndex = ExtractionHelper.startIndex(cargo.getId());
        appendMessage(cargo, startIndex);
    }

    @Override
    public int notCacheSize() {
        return NOT_CACHE_THRESHOLD;
    }

    @Override
    public void clearCache() {
        lruHashMap.clear();
    }
}
