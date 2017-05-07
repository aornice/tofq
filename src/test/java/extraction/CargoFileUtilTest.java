package extraction;

import deposition.LocalDepositionNonSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.CargoFileUtil;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.concurrent.*;

/**
 * Created by shen on 2017/4/28.
 */
public class CargoFileUtilTest {

    private static final String TOPIC_NAME = "test_topic1";
    TopicCenter topicCenter;
    CargoDeposition deposition;

    @Before
    public void init() {
        topicCenter = LocalTopicCenter.getInstance();
        PublicMethods.initFields();

        deposition = new LocalDepositionNonSingleton();
        deposition.start();

        topicCenter.register(TOPIC_NAME);
    }

    private void depositCargoes(Topic topic, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), ("message" + i).getBytes()));
        }
    }

    @Test
    public void dateCompare() throws InterruptedException, ExecutionException {
        int cargoNum  = 100;
        CountDownLatch latch = new CountDownLatch(cargoNum);

        deposition.addDepositionListener((topic, cargoId) -> {
            PublicMethods.notified(topic, latch, cargoNum);
        });

        depositCargoes(topicCenter.getTopic(TOPIC_NAME), cargoNum);


        latch.await(20000, TimeUnit.MILLISECONDS);

        String fileName = topicCenter.topicNewestFileShortName(TOPIC_NAME);
        Calendar calendar = Calendar.getInstance();
        String today = CargoFileUtil.dateStr(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = CargoFileUtil.dateStr(calendar.getTime());
        assertEquals(0, CargoFileUtil.fileCompareDateStr(fileName, today));
        assertEquals(1, CargoFileUtil.fileCompareDateStr(fileName, yesterday));
        System.out.println("==================");
        System.out.println("Test Finish");
        System.out.println("==================");

    }

    @After
    public void cleanup() throws InterruptedException {
        PublicMethods.cleanupDeposition(deposition);
        topicCenter.remove(TOPIC_NAME);
    }
}
