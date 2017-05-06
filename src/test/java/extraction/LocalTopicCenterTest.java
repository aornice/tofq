package extraction;

import deposition.LocalDepositionNonSingleton;
import org.junit.*;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by drfish on 22/04/2017.
 */
public class LocalTopicCenterTest {

    private TopicCenter topicCenter;
    private static final String TOPIC_NAME_1 = "test_topic1";

    private CargoDeposition deposition;
    private static Topic topic1;
    private static final Cargo[] cargoes = new Cargo[10];


    @Before
    public void init(){
        topicCenter = LocalTopicCenter.getInstance();
        PublicMethods.initFields();

        deposition = new LocalDepositionNonSingleton();
        deposition.start();

        topicCenter.register(TOPIC_NAME_1);
        topic1 = topicCenter.getTopic(TOPIC_NAME_1);
    }

    private void depositCargoes() throws InterruptedException {
        for (int i = 0; i < cargoes.length; i++) {
            cargoes[i] = new Cargo(topic1, topic1.incrementAndGetId(), ("message" + i).getBytes());
            deposition.write(cargoes[i]);
        }
    }

    @Test
    public void topicInfo(){
        assertEquals(true, topicCenter.existsTopic(TOPIC_NAME_1));
        assertEquals(-1, topicCenter.getTopic(TOPIC_NAME_1).getMaxStoredId());
    }

    @Test
    public void readTopics() throws InterruptedException {
        deposition.addDepositionListener((topic, cargoId) -> {
            if (cargoId == cargoes.length-1){
                assertEquals(9, topicCenter.getTopic(TOPIC_NAME_1).getMaxStoredId());
                System.out.println(topicCenter.getTopic(TOPIC_NAME_1).getCount());
            }
        });
        depositCargoes();
    }

    @Test
    public void fileNames() throws InterruptedException {
        depositCargoes();

        assertEquals(topicCenter.topicOldestFile(TOPIC_NAME_1), topicCenter.iThFile(TOPIC_NAME_1, 0));

        assertEquals(topicCenter.topicNewestFile(TOPIC_NAME_1), topicCenter.topicsNewestFile().get(TOPIC_NAME_1));

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH,2);
        Date tomorrow = calendar.getTime();


        assertEquals(0, topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, today).size());
        assertEquals(1, topicCenter.dateRangedFiles(TOPIC_NAME_1, yesterday, tomorrow).size());
        assertEquals(1, topicCenter.dateRangedFiles(TOPIC_NAME_1, today, tomorrow).size());
    }

    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME_1);
        PublicMethods.cleanupDeposition(deposition);
    }

}
