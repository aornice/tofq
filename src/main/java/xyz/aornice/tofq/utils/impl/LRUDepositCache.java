package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.DepositCache;
import xyz.aornice.tofq.utils.FileLocater;
import xyz.aornice.tofq.utils.TopicCenter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shen on 2017/4/14.
 */
public class LRUDepositCache implements DepositCache {

    private TopicCenter topicCenter = LocalTopicCenter.newInstance();

    private Harbour harbour;

    private FileLocater fileLocater;

    private static final int SHIFT_COUNT = 32;

    // cache capacity for each topic
    private static final int INIT_CAPACITY = 64;

    private LRUHashMap lruHashMap;

    private static volatile LRUDepositCache instance;

    public static DepositCache newInstance(){
        if (instance == null){
            synchronized (LocalFileLocator.class){
                if (instance == null){
                    instance = new LRUDepositCache(INIT_CAPACITY);
                }
            }
        }
        return instance;
    }

    private LRUDepositCache(int initCapacity){
        this.lruHashMap = new LRUHashMap(initCapacity);
        this.harbour = harbour;
        this.fileLocater = LocalFileLocator.newInstance();
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
    public List<byte[]> get(Topic topic, int fileIndex) {
        long topicID = topicCenter.topicInnerID(topic.getName());
        long hashValue = fileIndex<<SHIFT_COUNT+topicID;

        List<byte[]> fileContent = lruHashMap.get(hashValue);

        if (fileContent == null) {
            fileContent = harbour.get(fileLocater.fileNameByIndex(topic.getName(), fileIndex), 0, FileLocater.MESSAGES_PER_FILE);
            lruHashMap.put(hashValue, fileContent);
        }

        return fileContent;
    }
}
