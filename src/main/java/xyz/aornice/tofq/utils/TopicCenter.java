package xyz.aornice.tofq.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Scan all topics
 *
 * Created by cat on 2017/4/10.
 */
public class TopicCenter {
    private static ConcurrentHashMap<String,String> topicPathMap = new ConcurrentHashMap<>();
    private static Set<String> topics = topicPathMap.keySet();
    private static Path topicFolder = Paths.get("/Volumes/HDD/link/adventure/projects/315QueueFiles/testTopicFolder");

    private static final String FILE_SEPERATOR = System.getProperty("file.separator");


    static{
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (attr.isDirectory()) {
                    String topicName = file.getFileName().toString();
                    topicPathMap.put(topicName, topicFolder+FILE_SEPERATOR+topicName);
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

    public static Set<String> getTopics() {
        return topics;
    }

    public static boolean register(String topic) throws SecurityException{
        // return false if already exists
        if (topics.contains(topic)){
            return false;
        }

        // make the topic folder
        boolean created = new File(topicFolder + FILE_SEPERATOR + topic).mkdir();
        if (created) {
            topicPathMap.put(topic, topicFolder + FILE_SEPERATOR + topic);
        }
        return created;
    }

    public static Path getTopicFolder() {
        return topicFolder;
    }

    public static String getFileSeperator() {
        return FILE_SEPERATOR;
    }

    public static String getPath(String topic){
        return topicPathMap.get(topic);
    }
}
