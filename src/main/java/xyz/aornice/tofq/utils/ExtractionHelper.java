package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;

import java.util.List;

/**
 * Created by shen on 2017/4/16.
 */
public interface ExtractionHelper {
    /**
     * Calculate index of the file this message belongs to
     *
     * @param topic
     * @param index  the message index
     * @return       return null if the index is out of current bound or the topic does not exist
     */
    String fileName(Topic topic, long index);

    /**
     * Calculate the relative offset of a message in file
     *
     * @param index
     * @return
     */
    int messageOffset(long index);


    static long startIndex(long msgIndex) {
        return (msgIndex >> TopicFileFormat.Offset.CAPABILITY_POW) << TopicFileFormat.Offset.CAPABILITY_POW;
    }

    static long nextStartIndex(long msgIndex) {
        return ((msgIndex >> TopicFileFormat.Offset.CAPABILITY_POW)+1) << TopicFileFormat.Offset.CAPABILITY_POW ;
    }

    List<Long> msgByteOffsets(Topic topic, long startIndex);

    List<byte[]> read(Topic topic, long msgFromInd, long msgToInd);

    long startIndex(Topic topic, int iThFile);

}
