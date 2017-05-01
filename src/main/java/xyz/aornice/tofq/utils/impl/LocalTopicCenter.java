package xyz.aornice.tofq.utils.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat.FileName;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicChangeListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scan all topicNames
 * <p>
 * Created by cat on 2017/4/10.
 */
public class LocalTopicCenter implements TopicCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTopicCenter.class);

    /**
     * store inner topic info, such as topic path, inner id, etc
     */
    private static class InnerTopicInfo {
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

    private static ConcurrentHashMap<String, InnerTopicInfo> topicPathMap = new ConcurrentHashMap<>();
    private static Set<String> topicNames = topicPathMap.keySet();
    private static ConcurrentHashMap<String, Topic> topicObjMap = new ConcurrentHashMap<>();
    private static Path topicFolder = Paths.get(CargoFileUtil.getTopicRoot());

    private static Queue<TopicChangeListener> topicListeners = new ConcurrentLinkedQueue<>();

    private Harbour harbour = new LocalHarbour();


    private static final int INIT_TOPIC_FILES = 128;


    // topic file full path approximately takes 2 times the memory of file name
    // but the concat time elapse is 11 times the time of get full path directly
    private static Map<String, List<String>> topicFileFullNameMap = new HashMap<>();

    private static Map<String, List<String>> topicFileNameMap = new HashMap<>();

    public static TopicCenter getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton{
        static TopicCenter INSTANCE = new LocalTopicCenter();
    }


    private LocalTopicCenter() {
        for (String topicName : topicNames) {
            ArrayList<String> fileFullNames = createFileList();

            ArrayList<String> fileNames = createFileList();

            Path topicFolder = Paths.get(getTopicFolder(topicName));
            SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    if (attr.isRegularFile()) {
                        String fileName = file.getFileName().toString();
                        fileFullNames.add(topicFolder + File.separator + fileName);
                        fileNames.add(fileName);
                    }
                    return super.visitFile(file, attr);
                }
            };

            try {
                Files.walkFileTree(topicFolder, Collections.EMPTY_SET, 1, finder);

                // file name format is YYYYMMDD{Number}SUFFIX
                fileFullNames.sort(CargoFileUtil.getFileSortComparator());
                fileNames.sort(CargoFileUtil.getFileSortComparator());

                topicFileFullNameMap.put(topicName, fileFullNames);
                topicFileNameMap.put(topicName, fileNames);

                topicObjMap.put(topicName, new Topic(topicName, fileFullNames.get(fileFullNames.size() - 1)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static {
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (attr.isDirectory()) {
                    String topicName = file.getFileName().toString();
                    topicPathMap.put(topicName, new InnerTopicInfo(topicFolder + CargoFileUtil.getFileSeperator() + topicName));

                    // load old topic info
                    String folderName = topicFolder + CargoFileUtil.getFileSeperator() + topicName;
                    String fileName = folderName + CargoFileUtil.getFileSeperator() + FileName.DATE_FORMAT.format(Calendar.getInstance().getTime()) + FileName.START_IND + FileName.SUFFIX;
                    Topic createdTopic = new Topic(topicName, fileName);
                    topicObjMap.put(topicName, createdTopic);
                    topicNames = topicPathMap.keySet();
                }

                return super.visitFile(file, attr);
            }
        };

        // check if the basic topicFolder is created
        File file = topicFolder.toFile();
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (!isCreated) {
                LOGGER.error("creating topic folder failed!");
            }
        }

        try {
            // not scanning symlink path, max depth is 1
            java.nio.file.Files.walkFileTree(topicFolder, Collections.EMPTY_SET, 1, finder);
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
        return topicObjMap.get(topicName);
    }

    @Override
    public boolean register(String topicName) throws SecurityException {
        // return false if already exists
        if (topicNames.contains(topicName)) {
            return false;
        }

        // make the topic folder
        String folderName = topicFolder + CargoFileUtil.getFileSeperator() + topicName;
        File folder = new File(folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            boolean folderCreated = new File(folderName).mkdir();
            if (!folderCreated) {
                return false;
            }
        }

        String fileName = CargoFileUtil.dateStr(Calendar.getInstance().getTime()) + FileName.START_IND + FileName.SUFFIX;
        String fileFullName = folderName + CargoFileUtil.getFileSeperator() + fileName;

        boolean fileCreated = harbour.create(fileFullName);

        if (!fileCreated) {
            // not delete the folder: maybe there are some files in the folder
            return false;
        }

        topicPathMap.put(topicName, new InnerTopicInfo(topicFolder + CargoFileUtil.getFileSeperator() + topicName));

        Topic createdTopic = new Topic(topicName, fileFullName);

        topicObjMap.put(topicName, createdTopic);
        topicAdded(createdTopic);


        // update topicFileFullNameMap and topicFileNameMap
        topicFileFullNameMap.put(topicName, Arrays.asList(fileFullName));
        topicFileNameMap.put(topicName, Arrays.asList(fileName));

        return true;
    }

    @Override
    public boolean remove(String topicName) {
        String folderName = getTopicFolder(topicName);
//        String folderName = topicFolder + CargoFileUtil.getFileSeperator() + topicName;
        // delete the topic folder
        boolean fileDeleted = harbour.remove(folderName);
        if (!fileDeleted) {
            return false;
        }
        // clean the cache
        topicPathMap.remove(topicName);
        topicObjMap.remove(topicName);
        topicFileNameMap.remove(topicName);
        topicFileFullNameMap.remove(topicName);

        Topic topic = topicObjMap.get(topicName);
        topicDeleted(topic);
        return true;
    }

    @Override
    public String getTopicFolder(String topic) {
        return topicPathMap.get(topic).getPath();
    }

    @Override
    public boolean existsTopic(String topic) {
        return topicNames.contains(topic);
    }

    @Override
    public int topicInnerID(String topic) {
        return topicPathMap.get(topic).getInnerID();
    }

    @Override
    public void addListener(TopicChangeListener listener) {
        topicListeners.add(listener);
    }

    private void topicAdded(Topic newTopic) {
        topicListeners.stream().forEach(listener -> listener.topicAdded(newTopic));
    }

    private void topicDeleted(Topic topic) {
        topicListeners.stream().forEach(listener -> listener.topicDeleted(topic));
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
        List<String> files = topicFileFullNameMap.get(topic);
        if (files == null) {
            files = createFileList();
            topicFileFullNameMap.put(topic, files);
        }
        // the new file must be the last file
        files.add(filename);
    }

    @Override
    public Map<String, String> topicsNewestFile() {
        HashMap<String, String> rst = new HashMap<>(topicFileFullNameMap.size());
        for (Map.Entry<String, List<String>> e : topicFileFullNameMap.entrySet()) {
            List<String> files = e.getValue();
            rst.put(e.getKey(), files.get(files.size() - 1));
        }
        return rst;
    }

    @Override
    public String topicNewestFile(String topicName) {
        List<String> files = topicFileFullNameMap.get(topicName);
        return files.get(files.size() - 1);
    }

    @Override
    public String topicOldestFile(String topicName) {
        return topicFileFullNameMap.get(topicName).get(0);
    }

    @Override
    public String topicNewestFileShortName(String topicName) {
        List<String> files = topicFileNameMap.get(topicName);
        return files.get(files.size()-1);
    }

    @Override
    public String topicOldestFileShortName(String topicName) {
        return topicFileNameMap.get(topicName).get(0);
    }

    @Override
    public String iThFile(String topic, int i) {
        if (i < topicFileFullNameMap.get(topic).size()) {
            return topicFileFullNameMap.get(topic).get(i);
        }
        return null;
    }

    /**
     * search from end
     *
     * @param topic
     * @param from
     * @param to    exclusive
     * @return
     */
    @Override
    public List<String> dateRangedFiles(String topic, Date from, Date to) {
        List<String> files = topicFileNameMap.get(topic);
        int endInd = files.size() - 1;
        String toStr = CargoFileUtil.dateStr(to);
        String fromStr = CargoFileUtil.dateStr(from);
        for (int i = endInd; i >= 0; i--) {
            int compareRes = CargoFileUtil.fileCompareDateStr(files.get(i), toStr);
            if (compareRes >= 0) {
                endInd = i-1;
            } else {
                break;
            }
        }

        int startInd = 0;
        for (int i = endInd; i >= 0; i--) {
            int compareRes = CargoFileUtil.fileCompareDateStr(files.get(i), fromStr);
            if (compareRes >= 0) {
                startInd = i;
            } else {
                break;
            }
        }

        return topicFileFullNameMap.get(topic).subList(startInd, endInd + 1);
    }

}
