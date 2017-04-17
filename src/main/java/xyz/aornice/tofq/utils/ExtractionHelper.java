package xyz.aornice.tofq.utils;

import java.util.List;

/**
 * Created by shen on 2017/4/16.
 */
public interface ExtractionHelper {

    int MESSAGES_POW =10;
    // the message count in each file, must be 2^n, because used the fast module method: n & (m-1)
    int MESSAGES_PER_FILE = 1<<MESSAGES_POW;

    /**
     * Calculate index of the file this message belongs to
     *
     * @param topic
     * @param index  the message index
     * @return       return null if the index is out of current bound or the topic does not exist
     */
    String fileName(String topic, long index);

    /**
     * Calculate the relative offset of a message in file
     *
     * @param index
     * @return
     */
    int messageOffset(long index);

    long[] msgByteOffsets(String topic, String fileName);

    int currentMsgCount(String topic, String fileName);

    List<byte[]> read(String topic, long msgFromInd, long msgToInd);

    long startIndex(String topic, String fileName);

    List<byte[]> readInRange(String topic, String fromFile, String toFile, int fileCount);
}
