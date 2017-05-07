import deposition.LocalDepositionNonSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import static org.junit.Assert.*;

/**
 * Created by drfish on 27/04/2017.
 */
public class FileExtensionTest {

    private static final String TOPIC_NAME = "test_topic1";
    TopicCenter topicCenter;
    CargoDeposition deposition;
    Topic topic;

    @Before
    public void init(){
        topicCenter = LocalTopicCenter.getInstance();
        LocalTopicCenter.TEST_InitFields();
        LocalHarbour.TEST_InitFields();

        deposition = new LocalDepositionNonSingleton();
        deposition.start();

        topicCenter.register(TOPIC_NAME);
        topic = topicCenter.getTopic(TOPIC_NAME);
    }

    @Test
    public void fileExtend() {
        deposition.addDepositionListener((t, cargoId)->{
            if (cargoId == 9999){
                String newestFile = topic.getNewestFile();
                assertEquals(TopicFileFormat.FileName.SUFFIX, newestFile.substring(newestFile.length()-4, newestFile.length()));
            }
        });
        for(int i=0;i<100000;i++) {
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), ("message" + i).getBytes()));
        }
    }

    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME);
        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }
    }
}
