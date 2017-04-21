package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.TopicFileFormat.*;

import java.util.*;

/**
 * Created by shen on 2017/4/16.
 */
public class LocalExtractionHelper implements ExtractionHelper {

    private static ExtractionHelper instance = new LocalExtractionHelper();

    private TopicCenter topicCenter = LocalTopicCenter.newInstance();

    private Map<String, Long> startIndexMap;

    private Harbour harbour;

    public static ExtractionHelper newInstance(){
        return instance;
    }

    private LocalExtractionHelper(){
        startIndexMap = new HashMap<>(topicCenter.getTopicNames().size());
        harbour = new LocalHarbour("path");
    }

    @Override
    public List<Long> msgByteOffsets(String topic, String fileName) {
        int msgCount = currentMsgCount(topic, fileName);

        List<Long> byteOffsets = harbour.getLongs(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), Header.SIZE_BYTE, msgCount);

        return byteOffsets;
    }

    @Override
    public int currentMsgCount(String topic, String fileName) {
        // if not the newest file, then current Msg Count is MESSAGE_PER_FILE
        if (CargoFileUtil.getFileSortComparator().compare(fileName, topicCenter.topicNewestFile(topic)) < 0){
            return Offset.CAPABILITY;
        }

        int count = harbour.getInt(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), Header.COUNT_OFFSET_BYTE);
        return count;
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
            List<Long> offsets = msgByteOffsets(topic, fileName);

            if (nextBound > msgToInd){
                nextBound = msgToInd+1;
            }
            int startInd = messageOffset(current);
            int endInd = messageOffset(nextBound-1);

            long startOffset = 0;
            if (startInd != 0){
                startOffset = offsets.get(startInd-1);
            }
            long endOffset = offsets.get(endInd);

            byte[] rawMsgs = harbour.get(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), startOffset, endOffset);

            for (int i=startInd; i<endInd; i++){
                // TODO msg may be bigger than INT.MAX
                // TODO use memory address later
                int from = i==startInd? 0: (int)(offsets.get(startInd-1)-startOffset);
                int to = (int)(offsets.get(startInd)-startOffset);
                msgs.add(Arrays.copyOfRange(rawMsgs, from, to));
            }

            current = nextBound;
        }while(nextBound < msgToInd);

        return msgs;
    }

    @Override
    public long startIndex(String topic, String fileName) {
        long startInd = harbour.getLong(CargoFileUtil.filePath(topicCenter.getPath(topic), fileName), Header.ID_START_OFFSET_BYTE);
        return startInd;
    }

    @Override
    public List<byte[]> readInRange(String topic, String fromFile, String toFile, int fileCount) {
        long fromInd = startIndex(topic, fromFile);
        long toInd = startIndex(topic, toFile)+Offset.CAPABILITY;

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
        return (int) (index & (Offset.CAPABILITY - 1));
    }

    @Override
    public long startIndex(long msgIndex) {
        return (msgIndex>> Offset.CAPABILITY_POW)<<Offset.CAPABILITY_POW;
    }

    private long nextBound(String topic, long index) {
        return (fileIndex(topic,index) + 1) << Offset.CAPABILITY_POW;
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
        return (int) ((index- startIndex(topic)) >> Offset.CAPABILITY_POW);
    }

    private long startIndex(String topic){
        Long startIndex = startIndexMap.get(topic);
        if (startIndex == null){
            String firstFile = topicCenter.iThFile(topic, 0);
            startIndex = startIndex(topic, firstFile);
        }

        return startIndex;
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
