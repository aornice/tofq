package deposition;

import deposition.mock.HarbourMock;
import deposition.mock.TopicCenterMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.depostion.support.LocalDeposition;

/**
 * Created by robin on 18/04/2017.
 */
public class LocalDepositionTest {

    LocalDeposition deposition;
    TopicCenterMock topicCenterMock;

    private static final Logger logger = LogManager.getLogger(LocalDepositionTest.class);

    @Before
    public void setUp() {
        Setting.BATCH_DEPOSITION_SIZE = 2;

        deposition = (LocalDeposition) LocalDeposition.getInstance();
        deposition.setHarbour(new HarbourMock());
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
