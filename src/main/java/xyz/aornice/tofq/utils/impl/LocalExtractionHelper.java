package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shen on 2017/4/16.
 */
public class LocalExtractionHelper implements ExtractionHelper {

    private static ExtractionHelper instance = new LocalExtractionHelper();

    private TopicCenter topicCenter = LocalTopicCenter.newInstance();

    private Harbour harbour = new LocalHarbour("path");

    private static final int MSG_COUNT_OFFSET = 0;
    private static final int INT_BYTE_NUM = 4;
    private static final int START_INDEX_OFFSET = 4;
    private static final int LONG_BYTE_NUM = 8;
    private static final int LONG_BYTE_SHIFT = (LONG_BYTE_NUM>>1)-1;

    // header size in byte
    private static final int HEADER_SIZE = 64;

    // offset is long, which takes 8 bytes
    private static final int OFFSET_LENGTH = 8;

    private static final int MSG_BLOCK_HEADER = HEADER_SIZE+OFFSET_LENGTH<< MESSAGES_POW;


    public static ExtractionHelper newInstance(){
        return instance;
    }

    private LocalExtractionHelper(){

    }

    @Override
    public long[] msgByteOffsets(String topic, String fileName) {
        byte[] bytes = harbour.get(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), HEADER_SIZE, MSG_BLOCK_HEADER);
        int msgCount = currentMsgCount(topic, fileName);

        long[] byteOffsets = new long[msgCount];

        for (int i=0;i<msgCount;i++){
            byteOffsets[i] = byte2long(bytes, i<<LONG_BYTE_SHIFT);
        }

        return byteOffsets;
    }

    @Override
    public int currentMsgCount(String topic, String fileName) {
        // if not the newest file, then current Msg Count is MESSAGE_PER_FILE
        if (CargoFileUtil.getFileSortComparator().compare(fileName, topicCenter.topicNewestFile(topic)) < 0){
            return MESSAGES_PER_FILE;
        }

        // int value takes 4 bytes
        byte[] bytes = harbour.get(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), MSG_COUNT_OFFSET, MSG_COUNT_OFFSET+INT_BYTE_NUM);
        int count = byte2int(bytes);
        return count;
    }

    private int byte2int(byte[] bytes){
        int value= 0;
        for (int i = 0; i < INT_BYTE_NUM; i++) {
            int shift= (INT_BYTE_NUM - 1 - i) << 3;
            value +=(bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    private long byte2long(byte[] bytes, int start){
        long value = 0;
        for(int i=0; i<LONG_BYTE_NUM; i++){
            int shift = (LONG_BYTE_NUM -1 - i) << 3;
            value += (bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     *
     * @param topic
     * @param msgFromInd
     * @param msgToInd    the ind range is not checked here
     * @return
     */
    @Override
    public List<byte[]> read(String topic ,long msgFromInd, long msgToInd) {
        long current = msgFromInd;
        long nextBound;

        List<byte[]> msgs = new ArrayList<>((int)(msgToInd-msgFromInd));

        do {
            nextBound = nextBound(topic, current);
            String fileName = fileName(topic, current);
            long[] offsets = msgByteOffsets(topic, fileName);

            if (nextBound > msgToInd){
                nextBound = msgToInd+1;
            }
            int startInd = messageOffset(current);
            int endInd = messageOffset(nextBound-1);

            long startOffset = 0;
            if (startInd != 0){
                startOffset = offsets[startInd-1];
            }
            long endOffset = offsets[endInd];

            byte[] rawMsgs = harbour.get(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), startOffset, endOffset);

            for (int i=startInd; i<endInd; i++){
                // TODO msg may be bigger than INT.MAX
                // TODO use memory address later
                int from = i==startInd? 0: (int)(offsets[startInd-1]-startOffset);
                int to = (int)(offsets[startInd]-startOffset);
                msgs.add(Arrays.copyOfRange(rawMsgs, from, to));
            }

            current = nextBound;
        }while(nextBound < msgToInd);

        return msgs;
    }

    @Override
    public long startIndex(String topic, String fileName) {
        byte[] bytes = harbour.get(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), START_INDEX_OFFSET, LONG_BYTE_NUM);
        return byte2long(bytes, 0);
    }

    @Override
    public List<byte[]> readInRange(String topic, String fromFile, String toFile, int fileCount) {
        List<byte[]> results = new ArrayList<>(fileCount << MESSAGES_POW);

        long fromInd = startIndex(topic, fromFile);
        long toInd = startIndex(topic, toFile)+MESSAGES_PER_FILE;

        return read(topic, fromInd, toInd);
    }


    /**
     * Calculate the relative offset of a message in file
     *
     * @param index
     * @return
     */
    @Override
    public int messageOffset(long index) {
        return (int) (index & (MESSAGES_PER_FILE - 1));
    }

    private long nextBound(String topic, long index) {
        return (fileIndex(topic,index) + 1) << MESSAGES_POW;
    }

    /**
     * TODO did not consider the case of deleting file
     * When consider deleting, file index can be calculated by minus a shift.
     * When delete file, the filename list in topicFileMap should also be adjusted.
     * <p>
     * TODO the file index is int, because java only permits at most Integer.MAX_VALUE elements in ArrayList
     *
     * @param topic
     * @param index
     * @return the file index
     */
    private int fileIndex(String topic, long index) {
        return (int) ((index-firstIndex(topic)) >> MESSAGES_POW);
    }

    private long firstIndex(String topic){
        String firstFile = topicCenter.iThFile(topic, 0);
        return startIndex(topic, firstFile);
    }

    /**
     * Calculate index of the file this message belongs to
     *
     * @param topic
     * @param index the message index
     * @return return null if the index is out of current bound or the topic does not exist
     */
    @Override
    public String fileName(String topic, long index) {
        if (!topicCenter.existsTopic(topic)) {
            return null;
        }

        int fileInd = fileIndex(topic, index);
        return topicCenter.iThFile(topic, fileInd);
    }
}
