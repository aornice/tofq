package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.utils.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shen on 2017/4/14.
 */
public class LRUDepositCache implements DepositCache {

    private TopicCenter topicCenter = LocalTopicCenter.newInstance();

    private ExtractionHelper extractionHelper = LocalExtractionHelper.newInstance();

    private static final int SHIFT_COUNT = 32;

    // cache capacity for each topic
    private static final int INIT_CAPACITY = 64;

    private LRUHashMap lruHashMap;

    private static volatile DepositCache instance = new LRUDepositCache(INIT_CAPACITY);

    public static DepositCache newInstance(){
        return instance;
    }

    private LRUDepositCache(int initCapacity){
        this.lruHashMap = new LRUHashMap(initCapacity);
    }

    private class LRUHashMap extends LinkedHashMap<Long, List<byte[]>> {

        public LRUHashMap(int initCapacity){
            super(initCapacity);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, List<byte[]>> eldest) {
            if (size()>INIT_CAPACITY) {
                return true;
            }
            return false;
        }
    }


    @Override
    public List<byte[]> get(Topic topic, long startIndex) {
        long topicID = topicCenter.topicInnerID(topic.getName());
        long hashValue = startIndex<<SHIFT_COUNT+topicID;

        List<byte[]> fileContent = lruHashMap.get(hashValue);

        if (fileContent == null) {
            fileContent = extractionHelper.read(topic.getName(), startIndex, startIndex+ ExtractionHelper.MESSAGES_PER_FILE);
            lruHashMap.put(hashValue, fileContent);
        }

        return fileContent;
    }
}
