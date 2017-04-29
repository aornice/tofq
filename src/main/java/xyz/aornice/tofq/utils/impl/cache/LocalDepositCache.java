package xyz.aornice.tofq.utils.impl.cache;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.*;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shen on 2017/4/14.
 */
public abstract class LocalDepositCache<T> implements DepositCache {

    private TopicCenter topicCenter = LocalTopicCenter.getInstance();

    private final int SHIFT_COUNT = 32;

    protected class LRUHashMap extends LinkedHashMap<Long, List<T>> {

        public LRUHashMap(int initCapacity){
            super(initCapacity);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, List<T>> eldest) {
            if (size()>getInitCapacity()) {
                return true;
            }
            return false;
        }
    }

    protected abstract int getInitCapacity();

    protected long hashValue(Topic topic, long startIndex){
        long topicID = topicCenter.topicInnerID(topic.getName());
        long hashValue = (startIndex<<SHIFT_COUNT) + topicID;

        return hashValue;
    }

}
