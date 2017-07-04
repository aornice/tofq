package xyz.aornice.tofq.utils.impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat.Header;
import xyz.aornice.tofq.TopicFileFormat.Offset;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.cache.ContentCache;
import xyz.aornice.tofq.utils.cache.OffsetCache;
import xyz.aornice.tofq.utils.cache.StartIndexCache;
import xyz.aornice.tofq.utils.cache.impl.FileContentCache;
import xyz.aornice.tofq.utils.cache.impl.FileOffsetCache;
import xyz.aornice.tofq.utils.cache.impl.FileStartIndexCache;

import java.util.*;

/**
 * Created by shen on 2017/4/16.
 */
public class LocalExtractionHelper implements ExtractionHelper {

    private TopicCenter topicCenter = LocalTopicCenter.getInstance();

    private Map<String, Long> startIndexMap;

    private Harbour harbour;

    private final boolean USE_CACHE = true;
    private OffsetCache offsetCache;
    private ContentCache contentCache;
    private StartIndexCache startIndexCache;

    public static ExtractionHelper getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        static LocalExtractionHelper INSTANCE = new LocalExtractionHelper();

        static {
            init(INSTANCE);
        }
    }

    private LocalExtractionHelper() {
        offsetCache = new FileOffsetCache();
        contentCache = new FileContentCache();
        startIndexCache = new FileStartIndexCache();
    }

    private static void init(LocalExtractionHelper instance) {
        instance.startIndexMap = new HashMap<>(instance.topicCenter.getTopicNames().size());
        instance.harbour = new LocalHarbour();

        instance.offsetCache.clearCache();
        instance.startIndexCache.clearCache();
        instance.contentCache.clearCache();
    }

    public static void TEST_InitInstance() {
        init(Singleton.INSTANCE);
    }

    @Override
    public List<Long> msgByteOffsets(Topic topic, long startIndex) {
        List<Long> byteOffsets;

        boolean cached = false;

        if (USE_CACHE) {
            byteOffsets = offsetCache.getCache(topic, startIndex);
            if (byteOffsets != null) {
                cached = true;
            }
        }

        if (!cached) {
            String fileName = fileName(topic, startIndex);
            int msgCount = fileMsgCount(topic.getName(), fileName);
            byteOffsets = harbour.getLongs(fileName, Header.SIZE_BYTE, msgCount);

            if (USE_CACHE) {
                offsetCache.putCache(topic, startIndex, byteOffsets);
            }
        }

        return byteOffsets;
    }

    private int fileMsgCount(String topic, String fileName) {
        // if not the newest file, then current Msg Count is MESSAGE_PER_FILE
        if (CargoFileUtil.getFileSortComparator().compare(fileName, topicCenter.topicNewestFile(topic)) < 0) {
            return Offset.CAPABILITY;
        }

        int count = harbour.getInt(fileName, Header.COUNT_OFFSET_BYTE);
        return count;
    }

    /**
     * @param topic
     * @param msgFromInd
     * @param msgToInd   the ind range is not checked here
     * @return
     */
    @Override
    public List<byte[]> read(Topic topic, long msgFromInd, long msgToInd) {
        long current = msgFromInd;
        long nextBound;

        List<byte[]> msgs = new ArrayList<>((int) (msgToInd - msgFromInd));

        // do not cache if reading too much files
        int approxFileCount = (int) (msgToInd - msgFromInd) / Offset.CAPABILITY;
        boolean cacheTooMuch = approxFileCount > contentCache.notCacheSize();

        do {
            nextBound = nextBound(topic, current);
            String fileName = fileName(topic, current);
            long startIndex = ExtractionHelper.startIndex(current);
            List<Long> offsets = msgByteOffsets(topic, startIndex);

            if (nextBound > msgToInd) {
                nextBound = msgToInd + 1;
            }
            int relativeStartInd = messageOffset(current);
            int relativeEndInd = messageOffset(nextBound - 1);

            long startOffset = 0;
            if (relativeStartInd != 0) {
                startOffset = offsets.get(relativeStartInd - 1);
            }
            long endOffset = offsets.get(relativeEndInd);

            List<byte[]> batchMsgs;

            boolean cached = false;

            if (USE_CACHE) {
                batchMsgs = contentCache.getCache(topic, ExtractionHelper.startIndex(current));
                if (batchMsgs != null) {
                    cached = true;
                    msgs.addAll(batchMsgs);
                }
            }

            if (!cached) {
                byte[] rawMsgs = harbour.get(filePath(topic, fileName), startOffset, endOffset);

                List<byte[]> listToAdd;
                if (!USE_CACHE || cacheTooMuch) {
                    listToAdd = msgs;
                } else {
                    batchMsgs = new ArrayList<>(relativeEndInd - relativeStartInd);
                    listToAdd = batchMsgs;
                }

                for (int i = relativeStartInd; i < relativeEndInd; i++) {
                    // TODO msg may be bigger than INT.MAX?
                    int from = i == relativeStartInd ? 0 : (int) (offsets.get(relativeStartInd - 1) - startOffset);
                    int to = (int) (offsets.get(relativeStartInd) - startOffset);
                    listToAdd.add(Arrays.copyOfRange(rawMsgs, from, to));
                }

                if (USE_CACHE && !cacheTooMuch) {
                    contentCache.putCache(topic, startIndex, batchMsgs);
                    msgs.addAll(batchMsgs);
                }
            }

            current = nextBound;
        } while (nextBound < msgToInd);

        return msgs;
    }

    private String filePath(Topic topic, String fileName) {
        return CargoFileUtil.filePath(topicCenter.getTopicFolder(topic.getName()), fileName);
    }

    @Override
    public long startIndex(Topic topic, int iThFile) {
        String fileName = topicCenter.topicIThFileShortName(topic.getName(), iThFile);
        String filePath = filePath(topic, fileName);

        Long startIndex;

        boolean cached = false;

        if (USE_CACHE) {
            startIndex = startIndexCache.getCache(topic, iThFile);
            if (startIndex != null) {
                cached = true;
            }
        }

        if (!cached) {
            startIndex = harbour.getLong(filePath, Header.ID_START_OFFSET_BYTE);
            if (USE_CACHE) {
                startIndexCache.putCache(topic, iThFile, startIndex);
            }
        }

        return startIndex;
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

    private long nextBound(Topic topic, long index) {
        return (fileIndex(topic, index) + 1) << Offset.CAPABILITY_POW;
    }

    /**
     * TODO did not consider the case of deleting file
     * When consider deleting, file index can be calculated by minus a shift.
     * When delete file, the filename list in topicFileMap should also be adjusted.
     *
     * @param topic
     * @param index
     * @return the file index
     */
    private int fileIndex(Topic topic, long index) {
        return (int) ((index - startIndex(topic)) >> Offset.CAPABILITY_POW);
    }

    private long startIndex(Topic topic) {
        Long startIndex = startIndexMap.get(topic);
        if (startIndex == null) {
            startIndex = startIndex(topic, 0);
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
    public String fileName(Topic topic, long index) {
        if (!topicCenter.existsTopic(topic.getName())) {
            return null;
        }

        int fileInd = fileIndex(topic, index);
        return topicCenter.iThFile(topic.getName(), fileInd);
    }
}
