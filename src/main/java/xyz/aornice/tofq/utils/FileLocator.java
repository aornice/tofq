package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 *
 * Locate the target file by index
 *
 * Created by cat on 2017/4/11.
 */
public class FileLocator{
    private static final int MESSAGES_POW =10;
    // the message count in each file, must be 2^n, because used the fast module method: n & (m-1)
    private static final long MESSAGES_PER_FILE = 2<<MESSAGES_POW;

    private static final int INIT_TOPIC_FILES = 128;

    // date length in file name
    private static final int DATE_LENGTH = 8;

    // file suffix length
    private static final int SUFFIX_LENGTH = 4;

    private static Map<String, ArrayList<String>> topicFileMap = new HashMap<>();

    static{
        for (String topic: TopicCenter.getTopics()){
            ArrayList<String> files = createFileList();

            Path topicFolder = Paths.get(TopicCenter.getPath(topic));
            SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
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
                java.nio.file.Files.walkFileTree(topicFolder, Collections.EMPTY_SET, 1, finder);

                // file name format is YYYYMMDD{Number}SUFFIX
                files.sort(new Comparator<String>() {
                    @Override
                    public int compare(String file1, String file2) {
                        // first compare the date
                        for (int i=0;i<DATE_LENGTH; i++) {
                            if (file1.charAt(i) != file2.charAt(i)){
                                return file1.charAt(i)-file2.charAt(i);
                            }
                        }
                        // smaller if in the same date and file name is shorter
                        if (file1.length() != file2.length()){
                            return file1.length() - file2.length();
                        }
                        return file1.compareTo(file2);
                    }
                });

                topicFileMap.put(topic, files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate the relative offset of a message in file
     *
     * @param index
     * @return
     */
    public static final long messageOffset(long index){
        return index&(MESSAGES_PER_FILE-1);
    }

    /**
     * Calculate index of the file this message belongs to
     *
     * @param topic
     * @param index  the message index
     * @return       return null if the index is out of current bound or the topic does not exist
     */
    public static String fileName(String topic, long index){
        if (!TopicCenter.existsTopic(topic)){
            return null;
        }

        int fileInd = fileIndex(index);
        ArrayList<String> files = topicFileMap.get(topic);
        if (fileInd < files.size()) {
            return files.get(fileInd);
        }else{
            return null;
        }
    }

    /**
     * TODO did not consider the case of deleting file
     * When consider deleting, file index can be calculated by minus a shift.
     * When delete file, the filename list in topicFileMap should also be adjusted.
     *
     * TODO the file index is int, because java only permits at most Integer.MAX_VALUE elements in ArrayList
     *
     *
     * @param index
     * @return      the file index
     */
    private static final int fileIndex(long index){
        return (int)(index >> MESSAGES_POW);
    }

    private static ArrayList<String> createFileList(){
        return new ArrayList<String>(INIT_TOPIC_FILES);
    }

    /**
     * Should register the new file when file is created
     * Since the writing operation is serial, this method should not be called in parallel.
     *
     * The new created topic will be registered when the first file is added under the topic
     *
     * @param topic
     * @param filename
     */
    public static void registerNewFile(String topic, String filename) {
        ArrayList<String> files = topicFileMap.get(topic);
        if (files == null){
            files = createFileList();
            topicFileMap.put(topic, files);
        }
        // the new file must be the last file
        files.add(filename);
    }

    public static long nextBound(long index){
        return (fileIndex(index)+1)<<MESSAGES_POW ;
    }

    public static String filePath(String topic, String fileName){
        return TopicCenter.getPath(topic)+TopicCenter.getFileSeperator()+fileName;
    }

    public static Map<String, String> topicsNewestFile() {
        HashMap<String, String> rst = new HashMap<>(topicFileMap.size());
        for (Map.Entry<String, ArrayList<String>> e: topicFileMap.entrySet()) {
            ArrayList<String> files = e.getValue();
            rst.put(e.getKey(), files.get(files.size() - 1));
        }
        return rst;
    }

}
