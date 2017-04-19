package deposition;

import deposition.mock.HarbourMock;
import deposition.mock.TopicCenterMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
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
        logger.debug("setup");
        deposition = (LocalDeposition) LocalDeposition.getInstance();
        deposition.setHarbour(new HarbourMock());
        topicCenterMock = new TopicCenterMock();
        deposition.setTopicCenter(topicCenterMock);
    }

    @After
    public void tearDown() {
        deposition.close();
        logger.debug("tearDown");
    }

    @Test
    public void write() throws InterruptedException {
        Topic topic = topicCenterMock.getTopic("testtopic");
        for (int i = 0; i < 10; i++) {
            String msg = "msg-" + i;
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), msg.getBytes()));
        }
        Thread.sleep(10);
        String msg = "msg-" + 10;
        deposition.write(new Cargo(topic, topic.incrementAndGetId(), msg.getBytes()));
        Thread.sleep(100);
    }
}
