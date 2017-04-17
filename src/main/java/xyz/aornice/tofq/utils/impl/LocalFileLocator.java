package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.utils.FileLocater;
import xyz.aornice.tofq.utils.TopicCenter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.nio.file.Files;

/**
 * Locate the target file by index
 * <p>
 * Created by cat on 2017/4/11.
 */
public class LocalFileLocator implements FileLocater {
    private static final int INIT_TOPIC_FILES = 128;

    private static Map<String, ArrayList<String>> topicFileMap = new HashMap<>();

    private static TopicCenter topicCenter = LocalTopicCenter.newInstance();

    private static FileLocater instance = new LocalFileLocator();

    public static FileLocater newInstance() {
        return instance;
    }

    private LocalFileLocator() {
        for (String topic : topicCenter.getTopics()) {
            ArrayList<String> files = createFileList();

            Path topicFolder = Paths.get(topicCenter.getPath(topic));
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

                topicFileMap.put(topic, files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
