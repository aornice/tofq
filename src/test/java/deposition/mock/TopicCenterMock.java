package deposition.mock;

import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.TopicChangeListener;

import java.util.*;

/**
 * Created by robin on 18/04/2017.
 */
public class TopicCenterMock implements TopicCenter {

    Topic topic = new Topic("testtopic", Setting.BASE_PATH + "testtopic/201704180.tofq", new LocalHarbour());

    @Override
    public Set<Topic> getTopics() {
        return new HashSet<>(Arrays.asList(topic));
    }

    @Override
    public Set<String> getTopicNames() {
        return null;
    }

    @Override
    public Topic getTopic(String topicName) {
        return topic;
    }

    @Override
    public boolean register(String topicName) {
        return false;
    }

    @Override
    public boolean remove(String topicName) {
        return false;
    }

    @Override
    public String getPath(String topicName) {
        return null;
    }

    @Override
    public boolean existsTopic(String topicName) {
        return false;
    }

    @Override
    public int topicInnerID(String topicName) {
        return 0;
    }

    @Override
    public void addListener(TopicChangeListener listener) {

    }

    @Override
    public void registerNewFile(String topic, String filename) {

    }

    @Override
    public Map<String, String> topicsNewestFile() {
        return null;
    }

    @Override
    public String topicNewestFile(String topic) {
        return null;
    }

    @Override
    public String topicOldestFile(String topic) {
        return null;
    }

    @Override
    public String iThFile(String topic, int i) {
        return null;
    }

    @Override
    public List<String> dateRangedFiles(String topic, Date from, Date to) {
        return null;
    }
}
