package xyz.aornice.tofq.utils;

import xyz.aornice.tofq.Topic;

/**
 * Created by shen on 2017/4/18.
 */
public interface TopicChangeListener {
    void topicAdded(Topic newTopic);

    void topicDeleted(Topic topic);
}
