package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

import java.util.List;

/**
 * Created by shen on 2017/4/14.
 */
public interface DepositCache<T> {
    /**
     *
     * @param topic
     * @param msgIndex
     * @return
     */
    List<T> getFileContent(Topic topic, long msgIndex);


}
