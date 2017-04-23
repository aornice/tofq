package impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import static org.junit.Assert.assertEquals;

/**
 * Created by drfish on 22/04/2017.
 */
public class LocalTopicCenterTest {
    private TopicCenter topicCenter;
    private static final String TOPIC_NAME_1 = "test_topic1";
    private static final String TOPIC_NAME_2 = "test_topic2";


    @Before
    public void init() {
        topicCenter = LocalTopicCenter.newInstance();
    }

    @Test
    public void createTopic() {
        assertEquals(true, topicCenter.register(TOPIC_NAME_1));
        assertEquals(true, topicCenter.register(TOPIC_NAME_2));
        assertEquals(false, topicCenter.register(TOPIC_NAME_2));
        assertEquals(TOPIC_NAME_1, topicCenter.getTopic(TOPIC_NAME_1).getName());
    }

    @After
    public void cleanup() {
        assertEquals(true, topicCenter.remove(TOPIC_NAME_1));
        assertEquals(true, topicCenter.remove(TOPIC_NAME_2));
    }

}
