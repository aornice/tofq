package xyz.aornice.tofq.utils.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat.FileName;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicChangeListener;
import xyz.aornice.tofq.utils.TopicFileChangeListener;

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
public class LocalTopicCenter implements TopicCenter, TopicFileChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTopicCenter.class);

    @Override
    public void newFileAdded(String topicName, String fileName) {
        if (topicNames.contains(topicName)) {
            String topicFolder = getTopicFolder(topicName);
            String filePath = topicFolder + File.separator + fileName;
            topicFileFullNameMap.get(topicName).add(filePath);
            topicFileNameMap.get(topicName).add(fileName);
        }
    }

    @Override
    public void fileRemoved(String topicName, String filePath) {
        if (topicNames.contains(topicName)) {
            topicFileFullNameMap.remove(topicName);
            topicFileNameMap.remove(topicName);
        }
    }

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

    private ConcurrentHashMap<String, InnerTopicInfo> topicPathMap;
    private Set<String> topicNames;
    private ConcurrentHashMap<String, Topic> topicObjMap;
    private Path topicRoot = Paths.get(CargoFileUtil.getTopicRoot());

    private Queue<TopicChangeListener> topicListeners;

    private Harbour harbour;


    private final int INIT_TOPIC_FILES = 128;


    // topic file full path approximately takes 2 times the memory of file name
    // but the concat time elapse is 11 times the time of get full path directly
    private Map<String, List<String>> topicFileFullNameMap;

    private Map<String, List<String>> topicFileNameMap;

    public static TopicCenter getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        static LocalTopicCenter INSTANCE = new LocalTopicCenter();

        static {
            init(INSTANCE);
        }
    }


    /**
     * Danger, only used in junit test
     */
    public static void TEST_InitFields() {
        init(Singleton.INSTANCE);
    }


    private static void initVariables(LocalTopicCenter instance) {
        instance.topicPathMap = new ConcurrentHashMap<>();
        instance.topicObjMap = new ConcurrentHashMap<>();
        instance.topicListeners = new ConcurrentLinkedQueue<>();
        instance.harbour = new LocalHarbour();
        instance.topicFileFullNameMap = new HashMap<>();
        instance.topicFileNameMap = new HashMap<>();
    }

    protected static void init(LocalTopicCenter instance) {
        initVariables(instance);

        // check if the basic topicRoot is created
        File file = instance.topicRoot.toFile();
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (!isCreated) {
                LOGGER.error("creating topic folder failed!");
            }
        }

        try {
            // not scanning symlink path, max depth is 1
            Files.walkFileTree(instance.topicRoot, Collections.EMPTY_SET, 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    if (attr.isDirectory()) {
                        String topicName = file.getFileName().toString();
                        instance.topicPathMap.put(topicName, new InnerTopicInfo(instance.topicRoot + CargoFileUtil.getFileSeperator() + topicName));
                    }

                    return super.visitFile(file, attr);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        for (Iterator<Map.Entry<String, InnerTopicInfo>> it = instance.topicPathMap.entrySet().iterator(); it.hasNext(); ) {

            Map.Entry<String, InnerTopicInfo> entry = it.next();
            String topicName = entry.getKey();

            ArrayList<String> fileFullNames = instance.createStringList();

            ArrayList<String> fileNames = instance.createStringList();

            Path topicFolder = Paths.get(instance.getTopicFolder(topicName));
            SimpleFileVisitor<Path> topicFilesWalk = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    if (attr.isRegularFile()) {
                        String fileName = file.getFileName().toString();
                        if (fileName.length() > 4) {
                            String suffix = fileName.substring(fileName.length() - 4, fileName.length());
                            if (suffix.equals(FileName.SUFFIX)) {
                                fileFullNames.add(topicFolder + File.separator + fileName);
                                fileNames.add(fileName);
                            }
                        }
                    }
                    return super.visitFile(file, attr);
                }
            };

            try {
                Files.walkFileTree(topicFolder, Collections.EMPTY_SET, 1, topicFilesWalk);

                // file name format is YYYYMMDD{Number}SUFFIX
                fileFullNames.sort(CargoFileUtil.getFileSortComparator());
                fileNames.sort(CargoFileUtil.getFileSortComparator());

                if (fileFullNames.size() == 0) {
                    it.remove();
                    continue;
                }
                instance.topicFileFullNameMap.put(topicName, fileFullNames);
                instance.topicFileNameMap.put(topicName, fileNames);

                instance.topicObjMap.put(topicName, new Topic(topicName, fileFullNames.get(fileFullNames.size() - 1)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        instance.topicNames = instance.topicPathMap.keySet();
    }

    private LocalTopicCenter() {
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
        String folderName = topicRoot + CargoFileUtil.getFileSeperator() + topicName;
        File folder = new File(folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            boolean folderCreated = new File(folderName).mkdir();
            if (!folderCreated) {
                return false;
            }
        }

        String fileName = CargoFileUtil.dateStr(Calendar.getInstance().getTime()) + FileName.START_IND + FileName.SUFFIX;
        String fileFullName = folderName + CargoFileUtil.getFileSeperator() + fileName;

        Topic createdTopic = new Topic(topicName, fileFullName);
//        createdTopic.newTopicFile();

        topicPathMap.put(topicName, new InnerTopicInfo(topicRoot + CargoFileUtil.getFileSeperator() + topicName));
        topicObjMap.put(topicName, createdTopic);

        topicAdded(createdTopic);

        List<String> fullNames = createStringList();
        List<String> names = createStringList();
        fullNames.add(fileFullName);
        names.add(fileName);
        // update topicFileFullNameMap and topicFileNameMap
        topicFileFullNameMap.put(topicName, fullNames);
        topicFileNameMap.put(topicName, names);

        return true;
    }

    @Override
    public boolean remove(String topicName) {
        String folderName = getTopicFolder(topicName);
        if (folderName == null) {
            return false;
        }
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
        InnerTopicInfo topicInfo = topicPathMap.get(topic);
        if (topicInfo == null) {
            return null;
        } else {
            return topicInfo.getPath();
        }
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
        if (topic != null) {
            topicListeners.stream().forEach(listener -> listener.topicDeleted(topic));
        }
    }

    private ArrayList<String> createStringList() {
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
            files = createStringList();
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
    public int topicFileCount(String topicName) {
        return topicFileNameMap.get(topicName).size();
    }


    @Override
    public String topicNewestFileShortName(String topicName) {
        List<String> files = topicFileNameMap.get(topicName);
        return files.get(files.size() - 1);
    }

    @Override
    public String topicOldestFileShortName(String topicName) {
        return topicFileNameMap.get(topicName).get(0);
    }

    @Override
    public String topicIThFileShortName(String topicName, int i) {
        return topicFileNameMap.get(topicName).get(i);
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
                endInd = i - 1;
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
