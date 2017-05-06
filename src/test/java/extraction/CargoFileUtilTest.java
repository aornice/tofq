package extraction;

import deposition.LocalDepositionNonSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.CargoFileUtil;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import static org.junit.Assert.*;

import java.util.Calendar;

/**
 * Created by shen on 2017/4/28.
 */
public class CargoFileUtilTest {

    private static final String TOPIC_NAME_1 = "test_topic1";
    TopicCenter topicCenter;
    CargoDeposition deposition;

    @Before
    public void init(){
        topicCenter = LocalTopicCenter.getInstance();
        LocalTopicCenter.TEST_InitFields();
        LocalHarbour.TEST_InitFields();

        deposition = new LocalDepositionNonSingleton();
        deposition.start();

        topicCenter.register(TOPIC_NAME_1);
    }

    private void depositCargoes(Topic topic, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), ("message"+i).getBytes()));
        }
    }

    @Test
    public void dateCompare() throws InterruptedException {
        deposition.addDepositionListener((topic, cargoId) -> {
            if (cargoId == 10){
                String fileName = topicCenter.topicNewestFileShortName(TOPIC_NAME_1);
                Calendar calendar = Calendar.getInstance();
                String today = CargoFileUtil.dateStr(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                String yesterday = CargoFileUtil.dateStr(calendar.getTime());
                assertEquals(0, CargoFileUtil.fileCompareDateStr(fileName, today));
                assertEquals(1, CargoFileUtil.fileCompareDateStr(fileName, yesterday));
            }
        });
        depositCargoes(topicCenter.getTopic(TOPIC_NAME_1), 100);
    }

    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME_1);
        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }
    }
}
