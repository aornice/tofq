package xyz.aornice.tofq.utils.impl.cache;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;

import java.util.List;

/**
 * Created by shen on 2017/4/21.
 */
public class FileOffsetCache extends LocalDepositCache<Long> implements MessageListener {
    private final int INIT_CAPACITY = 1024;

    private ExtractionHelper extractionHelper;

    private LRUHashMap lruHashMap;

    private FileOffsetCache(int initCapacity) {
        lruHashMap = new LRUHashMap(initCapacity);
        extractionHelper = LocalExtractionHelper.newInstance();
    }

    @Override
    public List<Long> getFileContent(Topic topic, long msgIndex) {
        long hash = hashValue(topic, msgIndex);
        List<Long> indexes= lruHashMap.get(hash);

        if (indexes == null) {
            String fileName = extractionHelper.fileName(topic.getName(), msgIndex);
            indexes = extractionHelper.msgByteOffsets(topic.getName(), fileName);
            lruHashMap.put(hash, indexes);
        }

        return indexes;
    }

    @Override
    protected int getInitCapacity() {
        return INIT_CAPACITY;
    }

    @Override
    public void messageAdded(Cargo cargo, long offset) {
        long hash = hashValue(cargo.getTopic(), cargo.getId());
        List<Long> indexes = lruHashMap.get(hash);
        if (indexes != null){
            indexes.add(offset);
        }
    }
}
