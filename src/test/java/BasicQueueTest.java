import deposition.LocalDepositionNonSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
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
    public void init() throws InterruptedException {
        topicCenter = LocalTopicCenter.getInstance();
        extraction = new LocalExtraction();
        deposition = new LocalDepositionNonSingleton();
        deposition.start();
        registerTopics();
        generateCargoes();
    }

    private void registerTopics() {
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
        deposition.addDepositionListener((topic, cargoId) -> System.out.println(extraction.read(topic, cargoId)));
        for (Cargo cargo : cargoes) {
            deposition.write(cargo);
        }
    }

    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME_1);
        topicCenter.remove(TOPIC_NAME_2);
        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }
    }
}
