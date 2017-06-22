package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.cache.Cache;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by shen on 2017/4/14.
 */
public abstract class AbstractExtractionCache<T> implements Cache<T> {

    private TopicCenter topicCenter = LocalTopicCenter.getInstance();

    private final int SHIFT_COUNT = 32;

    protected final int CAPACITY;

    protected final int NOT_CACHE_THRESHOLD;

    protected AbstractExtractionCache(int capacity, int notCacheThreshold) {
        this.CAPACITY = capacity;
        this.NOT_CACHE_THRESHOLD = notCacheThreshold;
    }

//    protected class LRUHashMap extends LinkedHashMap<Long, T> {
//
//        public LRUHashMap(int initCapacity){
//            super(initCapacity);
//        }
//
//        @Override
//        protected boolean removeEldestEntry(Map.Entry<Long, T> eldest) {
//            if (size()> getCapacity()) {
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public T get(Object key) {
//
//            return super.get(key);
//        }
//    }


    public class LRUHashMap {

        class Node {
            Long key;
            T value;
            Node pre;
            Node next;

            public Node(Long key, T value) {
                this.key = key;
                this.value = value;
            }
        }

        ConcurrentLinkedDeque<Node> linkedNodes;

        int capacity;
        HashMap<Long, Node> map = new HashMap<>();
        Node head = null;
        Node end = null;

        public LRUHashMap(int capacity) {
            this.capacity = capacity;
            this.linkedNodes = new ConcurrentLinkedDeque<>();
            // reused nodes
            for (int i = 0; i < capacity; i++) {
                this.linkedNodes.add(new Node(null, null));
            }
        }

        public T get(Long key) {
            Node n = map.get(key);
            if (n == null) {
                return null;
            } else {
                if (n != head) {
                    removeInList(n);
                    setHead(n);
                }
                return n.value;
            }
        }

        public void remove(Long key) {
            Node n = map.get(key);
            removeInList(n);
            map.remove(key);
            linkedNodes.push(n);
        }

        private void removeInList(Node n) {
            if (n == null) {
                return;
            }

            if (n.pre != null) {
                n.pre.next = n.next;
            } else {
                head = n.next;
            }

            if (n.next != null) {
                n.next.pre = n.pre;
            } else {
                end = n.pre;
            }

        }

        private void setHead(Node n) {
            n.next = head;
            n.pre = null;

            if (head != null)
                head.pre = n;

            head = n;

            if (end == null)
                end = head;
        }

        public void set(Long key, T value) {

            Node old = map.get(key);

            if (old != null) {
                old.value = value;

                if (old != head) {
                    removeInList(old);
                    setHead(old);
                }
            } else {
                if (map.size() >= capacity) {
                    removeInList(end);
                    remove(end.key);
                }

                Node created = linkedNodes.pop();
                created.key = key;
                created.value = value;

                setHead(created);

                map.put(key, created);
            }
        }

        public void clear() {
            map.clear();
            int i = linkedNodes.size();
            while (i < capacity) {
                linkedNodes.add(new Node(null, null));
                i++;
            }
        }
    }

    protected long hashValue(Topic topic, long startIndex) {
        long topicID = topicCenter.topicInnerID(topic.getName());
        long hashValue = (startIndex << SHIFT_COUNT) + topicID;

        return hashValue;
    }

}
