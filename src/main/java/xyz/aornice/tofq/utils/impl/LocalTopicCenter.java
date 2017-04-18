package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicUpdateListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Scan all topicNames
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
    private static Set<String> topicNames = topicPathMap.keySet();
    private static ConcurrentHashMap<String, Topic> topicObjMap = new ConcurrentHashMap<>();
    private static Path topicFolder = Paths.get(CargoFileUtil.getTopicRoot());

    private volatile static TopicCenter instance = new LocalTopicCenter();

    private static final int INIT_TOPIC_FILES = 128;

    private static Map<String, ArrayList<String>> topicFileMap = new HashMap<>();

    public static TopicCenter newInstance(){
        return instance;
    }

    private LocalTopicCenter(){
        for (String topicName : topicNames) {
            ArrayList<String> files = createFileList();

            Path topicFolder = Paths.get(getPath(topicName));
            SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    if (attr.isRegularFile()) {
                        String fileName = file.getFileName().toString();
                        files.add(fileName);
                    }
                    return super.visitFile(file, attr);
                }
            };

            try {
                Files.walkFileTree(topicFolder, Collections.EMPTY_SET, 1, finder);

                // file name format is YYYYMMDD{Number}SUFFIX
                files.sort(CargoFileUtil.getFileSortComparator());

                topicFileMap.put(topicName, files);
                topicObjMap.put(topicName, new Topic(topicName, files.get(files.size()-1)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static{
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (attr.isDirectory()) {
                    String topicName = file.getFileName().toString();
                    topicPathMap.put(topicName, new InnerTopicInfo(topicFolder+ CargoFileUtil.getFileSeperator()+topicName));
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
    public Set<Topic> getTopics() {
        return new HashSet<>(topicObjMap.values());
    }

    @Override
    public Set<String> getTopicNames() {
        return topicNames;
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }

    @Override
    public boolean register(String topic) throws SecurityException{
        // return false if already exists
        if (topicNames.contains(topic)){
            return false;
        }

        // make the topic folder
        boolean created = new File(topicFolder + CargoFileUtil.getFileSeperator() + topic).mkdir();
        if (created) {
            topicPathMap.put(topic, new InnerTopicInfo(topicFolder + CargoFileUtil.getFileSeperator() + topic));
        }
        return created;
    }

    @Override
    public String getPath(String topic){
        return topicPathMap.get(topic).getPath();
    }

    @Override
    public boolean existsTopic(String topic){
        return topicNames.contains(topic);
    }

    @Override
    public int topicInnerID(String topic) {
        return topicPathMap.get(topic).getInnerID();
    }

    //TODO
    @Override
    public void addListener(TopicUpdateListener listener) {
    }


    private static ArrayList<String> createFileList() {
        return new ArrayList<String>(INIT_TOPIC_FILES);
    }

    /**
     * Should register the new file when file is created
     * Since the writing operation is serial, this method should not be called in parallel.
     * <p>
     * The new created topic will be registered when the first file is added under the topic
     *
     * @param topic
     * @param filename
     */
    @Override
    public void registerNewFile(String topic, String filename) {
        ArrayList<String> files = topicFileMap.get(topic);
        if (files == null) {
            files = createFileList();
            topicFileMap.put(topic, files);
        }
        // the new file must be the last file
        files.add(filename);
    }

    @Override
    public Map<String, String> topicsNewestFile() {
        HashMap<String, String> rst = new HashMap<>(topicFileMap.size());
        for (Map.Entry<String, ArrayList<String>> e : topicFileMap.entrySet()) {
            ArrayList<String> files = e.getValue();
            rst.put(e.getKey(), files.get(files.size() - 1));
        }
        return rst;
    }

    @Override
    public String topicNewestFile(String topic) {
        List<String> files = topicFileMap.get(topic);
        return files.get(files.size() - 1);
    }

    @Override
    public String topicOldestFile(String topic) {
        return topicFileMap.get(topic).get(0);
    }

    @Override
    public String iThFile(String topic, int i) {
        if (i < topicFileMap.get(topic).size()){
            return topicFileMap.get(topic).get(i);
        }
        return null;
    }

    /**
     * search from end
     * @param topic
     * @param from
     * @param to    exclusice
     * @return
     */
    @Override
    public List<String> dateRangedFiles(String topic, Date from, Date to) {
        ArrayList<String> files = topicFileMap.get(topic);
        int endInd=files.size()-1;
        for(int i=files.size()-1; i>=0; i--){
            int compareRes = CargoFileUtil.compareToDate(files.get(i), to);
            if (compareRes >=0) {
                endInd --;
            }else{
                break;
            }
        }

        int startInd = 0;
        for (int i=endInd; i>=0; i--){
            int compareRes = CargoFileUtil.compareToDate(files.get(i), from);
            if (compareRes >= 0){
                startInd = i;
            }else{
                break;
            }
        }

        return files.subList(startInd, endInd+1);
    }
}
