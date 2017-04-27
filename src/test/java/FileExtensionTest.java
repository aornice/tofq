import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

/**
 * Created by drfish on 27/04/2017.
 */
public class FileExtensionTest {
    @Test
    public void fileExtend() {
        CargoDeposition deposition = LocalDeposition.getInstance();
//        deposition.start();
        TopicCenter topicCenter = LocalTopicCenter.newInstance();
        String topicName = "test_topic1";
        topicCenter.remove(topicName);
        topicCenter.register(topicName);
        Topic topic = topicCenter.getTopic(topicName);
        for(int i=0;i<100000;i++) {
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), ("message" + i).getBytes()));
        }
    }
}
