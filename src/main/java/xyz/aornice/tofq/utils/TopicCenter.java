package xyz.aornice.tofq.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by shen on 2017/4/14.
 */
public interface TopicCenter {
    Set<String> getTopics();

    boolean register(String topic);

    Path getTopicFolder();

    String getFileSeperator();

    String getPath(String topic);

    boolean existsTopic(String topic);

    int topicInnerID(String topic);
}
