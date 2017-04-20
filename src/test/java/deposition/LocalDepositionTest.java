package deposition;

import deposition.mock.TopicCenterMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;

/**
 * Created by robin on 18/04/2017.
 */
public class LocalDepositionTest {

    LocalDeposition deposition;
    TopicCenterMock topicCenterMock;

    private static final Logger logger = LoggerFactory.getLogger(LocalDepositionTest.class);

    @Before
    public void setUp() {
        Setting.BATCH_DEPOSITION_SIZE = 2;

        deposition = (LocalDeposition) LocalDeposition.getInstance();
        deposition.setHarbour(new LocalHarbour());
        topicCenterMock = new TopicCenterMock();
        deposition.setTopicCenter(topicCenterMock);
    }

    @After
    public void tearDown() {
        deposition.close();
    }

    @Test
    public void write() throws InterruptedException {
        Topic topic = topicCenterMock.getTopic("testtopic");
        long startId = topic.getStartId();
        for (long i = startId; i < startId + 10; i++) {
            String msg = "msg-" + i;
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), msg.getBytes()));
        }
        Thread.sleep(10);
        String msg = "msg-" + (startId + 10);
        deposition.write(new Cargo(topic, topic.incrementAndGetId(), msg.getBytes()));
        Thread.sleep(100);
    }
}
