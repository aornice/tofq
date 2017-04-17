package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

import java.util.List;

/**
 * Created by shen on 2017/4/14.
 */
public interface DepositCache {
    /**
     *
     * @param topic
     * @param startIndex
     * @return
     */
    List<byte[]> get(Topic topic, long startIndex);
}
