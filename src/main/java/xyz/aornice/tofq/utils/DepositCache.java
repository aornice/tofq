package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

import java.util.List;

/**
 * Created by shen on 2017/4/14.
 */
public interface DepositCache {
    /**
     * why using fileIndex instead of fileName? the comparison of String is slower than int
     * @param topic
     * @param fileIndex
     * @return
     */
    List<byte[]> get(Topic topic, int fileIndex);
}
