package xyz.aornice.tofq.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

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

    private static Map<String, AtomicReferenceArray<String>> topicFileMap = new HashMap<>();

    public static final long shift(long index){
        return index&(MESSAGES_PER_FILE-1);
    }

    public static String fileName(String topic, long index){
        return topicFileMap.get(topic).get(fileIndex(index));
    }

    /**
     * TODO did not consider the case of deleting file
     * When consider deleting, file index can be calculated by minus a shift.
     * When delete file, the filename list in topicFileMap should also be adjusted.
     *
     *
     * @param index
     * @return     return int because java only permits at most Integer.MAX_VALUE elements in ArrayList
     */
    private static final int fileIndex(long index){
        return (int)(index >> MESSAGES_POW);
    }

    /**
     * should register the new file when file is created
     * @param topic
     * @param filename
     */
    public static void registerNewFile(String topic, String filename) {
        AtomicReferenceArray<String> files = topicFileMap.get(topic);
        if (files == null){
            files = new AtomicReferenceArray<String>(INIT_TOPIC_FILES);
            topicFileMap.put(topic, files);
        }
        // the new file must be the last file
//        files.add(filename);
//        files.set();

    }

}
