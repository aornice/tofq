package xyz.aornice.tofq.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shen on 2017/4/14.
 */
public interface FileLocater {


    int MESSAGES_POW =10;
    // the message count in each file, must be 2^n, because used the fast module method: n & (m-1)
    long MESSAGES_PER_FILE = 2<<MESSAGES_POW;

    /**
     * Calculate the relative offset of a message in file
     *
     * @param index
     * @return
     */
     long messageOffset(long index);

    /**
     * Calculate index of the file this message belongs to
     *
     * @param topic
     * @param index  the message index
     * @return       return null if the index is out of current bound or the topic does not exist
     */
    String fileName(String topic, long index);


    /**
     * Should register the new file when file is created
     * Since the writing operation is serial, this method should not be called in parallel.
     *
     * The new created topic will be registered when the first file is added under the topic
     *
     * @param topic
     * @param filename
     */
    void registerNewFile(String topic, String filename);

    long nextBound(long index);

    String filePath(String topic, String fileName);

    Map<String, String> topicsNewestFile();

    String fileNameByIndex(String topic, int fileIndex);
}
