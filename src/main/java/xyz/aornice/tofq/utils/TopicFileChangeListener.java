package xyz.aornice.tofq.utils;

/**
 * Created by shen on 2017/5/4.
 */
public interface TopicFileChangeListener {
    void newFileAdded(String topicName, String filePath);

    void fileRemoved(String topicName, String filePath);
}
