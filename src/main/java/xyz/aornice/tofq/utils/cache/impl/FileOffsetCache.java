package xyz.aornice.tofq.utils.cache.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.cache.MessageListener;
import xyz.aornice.tofq.utils.cache.OffsetCache;
import xyz.aornice.tofq.utils.impl.AbstractExtractionCache;

import java.util.List;

/**
 * Created by shen on 2017/5/14.
 */
public class FileOffsetCache extends AbstractExtractionCache<List<Long>> implements OffsetCache,MessageListener {

    private LRUHashMap lruHashMap;

    private static final int capacity = 1024;
    private static final double prihibitRatio = 1;

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public List<Long> getCache(Topic topic, long startIndex) {
        long hash = hashValue(topic, startIndex);
        List<Long> offsets= lruHashMap.get(hash);

        return offsets;
    }

    @Override
    public void putCache(Topic topic, long startIndex, List<Long> offsets) {
        lruHashMap.set(hashValue(topic, startIndex), offsets);
    }


    public void appendOffset(Cargo cargo, long startIndex, long offset){
        long hash = hashValue(cargo.getTopic(), startIndex);
        List<Long> offsets = lruHashMap.get(hash);
        if (offsets != null){
            offsets.add(offset);
        }
    }

    public FileOffsetCache() {
        super(capacity, (int)(capacity* prihibitRatio));
        init();
    }

    private void init(){
        lruHashMap = new LRUHashMap(CAPACITY);
    }


    @Override
    public void messageAdded(Cargo cargo, long offset) {
        long startIndex = ExtractionHelper.startIndex(cargo.getId());
        appendOffset(cargo,startIndex, offset);
    }

    @Override
    public void clearCache() {
        lruHashMap.clear();
    }
}
