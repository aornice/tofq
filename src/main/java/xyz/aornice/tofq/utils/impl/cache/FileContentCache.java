package xyz.aornice.tofq.utils.impl.cache;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat.*;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;

import java.util.List;

/**
 * Created by shen on 2017/4/21.
 *
 *
 */
public class FileContentCache extends LocalDepositCache<byte[]> implements MessageListener{

    private final int INIT_CAPACITY = 64;

    private ExtractionHelper extractionHelper;

    private LRUHashMap lruHashMap;

    private FileContentCache(int initCapacity) {
        lruHashMap = new LRUHashMap(initCapacity);
        extractionHelper = LocalExtractionHelper.newInstance();
    }

    @Override
    public List<byte[]> getFileContent(Topic topic, long msgIndex) {
        long hash = hashValue(topic, msgIndex);
        List<byte[]> fileContent = lruHashMap.get(hash);

        long startIndex = extractionHelper.startIndex(msgIndex);

        if (fileContent == null) {
            fileContent = extractionHelper.read(topic.getName(), startIndex, startIndex + Offset.CAPABILITY);
            lruHashMap.put(hash, fileContent);
        }

        return fileContent;
    }

    @Override
    protected int getInitCapacity() {
        return INIT_CAPACITY;
    }

    @Override
    public void messageAdded(Cargo cargo, long offset) {
        long hash = hashValue(cargo.getTopic(), cargo.getId());
        List<byte[]> fileContent = lruHashMap.get(hash);
        if (fileContent != null){
            fileContent.add(cargo.getData());
        }
    }
}
