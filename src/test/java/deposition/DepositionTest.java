package deposition;

import deposition.mock.HarbourMock;
import deposition.mock.TopicCenterMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.support.AbstractDeposition;

/**
 * Created by robin on 18/04/2017.
 */
public class DepositionTest {

    AbstractDeposition deposition;
    TopicCenterMock topicCenterMock;

    private static final Logger logger = LoggerFactory.getLogger(DepositionTest.class);

    @Before
    public void setUp() {
        Setting.BATCH_DEPOSITION_SIZE = 2;

        topicCenterMock = new TopicCenterMock();
        deposition = new Deposition();
        deposition.start();
    }

    @After
    public void tearDown() {
        deposition.shutdown();
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
        Thread.sleep(50);
    }

    class Deposition extends AbstractDeposition {
        public Deposition() {
            setHarbour(new HarbourMock());
            setTopicCenter(topicCenterMock);
        }
    }
}

