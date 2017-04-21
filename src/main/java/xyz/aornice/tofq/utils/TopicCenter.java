package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shen on 2017/4/14.
 */
public interface TopicCenter {
    Set<Topic> getTopics();

    Set<String> getTopicNames();

    Topic getTopic(String topicName);

    boolean register(String topicName);

    String getPath(String topicName);

    boolean existsTopic(String topicName);

    int topicInnerID(String topicName);

    void addListener(TopicChangeListener listener);

    /**
     * Should register the new file when file is created
     * Since the writing operation is serial, this method should not be called in parallel.
     *
     * The new created topic will be registered when the first file is added under the topic
     *
     * @param topic
     * @param filename
     */
    void registerNewFile(String topic, String filename);

    Map<String, String> topicsNewestFile();

    String topicNewestFile(String topic);

    String topicOldestFile(String topic);

    String iThFile(String topic, int i);

    /**
     *
     * @param topic
     * @param from
     * @param to    exclusice
     * @return
     */
    List<String> dateRangedFiles(String topic, Date from, Date to);
}
