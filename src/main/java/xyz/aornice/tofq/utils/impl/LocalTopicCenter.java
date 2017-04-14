package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Scan all topics
 *
 * Created by cat on 2017/4/10.
 */
public class LocalTopicCenter implements TopicCenter {

    /**
     * store inner topic info, such as topic path, inner id, etc
     */
    private static class InnerTopicInfo{
        private static AtomicInteger nextID = new AtomicInteger(0);
        private int innerID;
        private String path;

        public InnerTopicInfo(String path) {
            this.path = path;
            this.innerID = nextID.getAndIncrement();
        }

        public int getInnerID() {
            return innerID;
        }

        public String getPath() {
            return path;
        }
    }

    private static ConcurrentHashMap<String,InnerTopicInfo> topicPathMap = new ConcurrentHashMap<>();
    private static Set<String> topics = topicPathMap.keySet();
    private static Path topicFolder = Paths.get("/Users/shen/workspace/项目/315QueueFiles/testTopicFolder");

    private static final String FILE_SEPERATOR = System.getProperty("file.separator");

    private volatile static LocalTopicCenter instance;

    public static TopicCenter newInstance(){
        if (instance==null){
            synchronized (LocalTopicCenter.class){
                if (instance==null){
                    instance = new LocalTopicCenter();
                }
            }
        }
        return instance;
    }

    private LocalTopicCenter(){

    }


    static{
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (attr.isDirectory()) {
                    String topicName = file.getFileName().toString();
                    topicPathMap.put(topicName, new InnerTopicInfo(topicFolder+FILE_SEPERATOR+topicName));
                }

                return super.visitFile(file, attr);
            }
        };

        try {
            // not scanning symlink path, max depth is 1
            java.nio.file.Files.walkFileTree(topicFolder,Collections.EMPTY_SET,1, finder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getTopics() {
        return topics;
    }

    @Override
    public boolean register(String topic) throws SecurityException{
        // return false if already exists
        if (topics.contains(topic)){
            return false;
        }

        // make the topic folder
        boolean created = new File(topicFolder + FILE_SEPERATOR + topic).mkdir();
        if (created) {
            topicPathMap.put(topic, new InnerTopicInfo(topicFolder + FILE_SEPERATOR + topic));
        }
        return created;
    }

    @Override
    public Path getTopicFolder() {
        return topicFolder;
    }

    @Override
    public String getFileSeperator() {
        return FILE_SEPERATOR;
    }

    @Override
    public String getPath(String topic){
        return topicPathMap.get(topic).getPath();
    }

    @Override
    public boolean existsTopic(String topic){
        return topics.contains(topic);
    }

    @Override
    public int topicInnerID(String topic) {
        return topicPathMap.get(topic).getInnerID();
    }
}
