import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.impl.LocalExtraction;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

/**
 * Created by drfish on 09/04/2017.
 */
public class BasicQueueTest {
    private TopicCenter topicCenter;
    private CargoDeposition deposition;
    private CargoExtraction extraction;
    private static final String TOPIC_NAME_1 = "test_topic1";
    private static final String TOPIC_NAME_2 = "test_topic2";
    private static Topic topic1;
    private static Topic topic2;
    private static final Cargo[] cargoes = new Cargo[10];

    @Before
    public void init() {
        topicCenter = LocalTopicCenter.getInstance();
        extraction = new LocalExtraction();
        deposition = LocalDeposition.getInstance();
        deposition.start();
        registerTopics();
        generateCargoes();
    }

    private void registerTopics() {
//        topicCenter.remove(TOPIC_NAME_1);
        topicCenter.register(TOPIC_NAME_1);
        topicCenter.register(TOPIC_NAME_2);
        topic1 = topicCenter.getTopic(TOPIC_NAME_1);
        topic2 = topicCenter.getTopic(TOPIC_NAME_2);
    }

    private void generateCargoes() {
        for (int i = 0; i < cargoes.length; i++) {
            cargoes[i] = new Cargo(topic1, topic1.incrementAndGetId(), ("message" + i).getBytes());
        }
    }

    @Test
    public void deposit() throws InterruptedException {
        for (Cargo cargo : cargoes) {
            deposition.write(cargo);
        }
        Thread.sleep(100);
        for (int i = 0; i < 10; i++) {
            Cargo cargo = extraction.read(topic1, i);
            System.out.println(cargo);
        }
    }

    @After
    public void cleanup() {
//        topicCenter.remove(TOPIC_NAME_1);
//        topicCenter.remove(TOPIC_NAME_2);
    }
}
