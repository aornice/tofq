package extraction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
        topicCenter = LocalTopicCenter.getInstance();
    }

    @Test
    public void createTopic() {
//        assertEquals(true, topicCenter.register(TOPIC_NAME_1));
//        assertEquals(true, topicCenter.register(TOPIC_NAME_2));
//        assertEquals(false, topicCenter.register(TOPIC_NAME_2));
//        assertEquals(TOPIC_NAME_1, topicCenter.getTopic(TOPIC_NAME_1).getName());
//        assertEquals(-1, topicCenter.getTopic(TOPIC_NAME_1).getMaxStoredId());
//
//        assertEquals(true, topicCenter.existsTopic(TOPIC_NAME_1));
//        assertEquals(true, topicCenter.existsTopic(TOPIC_NAME_2));
    }


    @Test
    public void readTopics(){
//        assertEquals(2, topicCenter.getTopics().size());
//        assertEquals(-1, topicCenter.getTopic(TOPIC_NAME_1).getMaxStoredId());
//        assertEquals(0, topicCenter.getTopic(TOPIC_NAME_1).getCount());
    }

    @Test
    public void fileNames(){
//        assertEquals("testtopic/topic_test_topic1/201704290.tof", topicCenter.topicNewestFile(TOPIC_NAME_1));
//        assertEquals("testtopic/topic_test_topic1/201704280.tof",topicCenter.topicOldestFile(TOPIC_NAME_1));
//        assertEquals("201704290.tof",topicCenter.topicNewestFileShortName(TOPIC_NAME_1));
//        assertEquals("201704280.tof",topicCenter.topicOldestFileShortName(TOPIC_NAME_1));
//
//        assertEquals(topicCenter.topicOldestFile(TOPIC_NAME_1), topicCenter.iThFile(TOPIC_NAME_1, 0));
//
//        assertEquals("testtopic/topic_test_topic1", topicCenter.getTopicFolder(TOPIC_NAME_1));
//
//        assertEquals(topicCenter.topicNewestFile(TOPIC_NAME_1), topicCenter.topicsNewestFile().get(TOPIC_NAME_1));
//        assertEquals(topicCenter.topicNewestFile(TOPIC_NAME_2), topicCenter.topicsNewestFile().get(TOPIC_NAME_2));
//
//        Calendar calendar = Calendar.getInstance();
//        Date today = calendar.getTime();
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//        Date yesterday = calendar.getTime();
//        calendar.add(Calendar.DAY_OF_MONTH,2);
//        Date tomorrow = calendar.getTime();
//
//
//        System.out.println(Arrays.toString(topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, today).toArray()));
//        System.out.println(Arrays.toString(topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, tomorrow).toArray()));
//        System.out.println(Arrays.toString(topicCenter.dateRangedFiles(TOPIC_NAME_1, today, tomorrow).toArray()));


//        assertEquals(1,topicCenter.dateRangedFiles(TOPIC_NAME_1, today, tomorrow).size());
//        assertEquals(0, topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, today).size());
//        assertEquals(1, topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, tomorrow).size());

    }

    @After
    public void cleanup() {
//        assertEquals(true, topicCenter.remove(TOPIC_NAME_1));
//        assertEquals(true, topicCenter.remove(TOPIC_NAME_2));
    }

}
