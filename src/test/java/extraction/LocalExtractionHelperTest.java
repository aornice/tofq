package extraction;

import deposition.LocalDepositionNonSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.impl.LocalExtraction;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by cat on 2017/4/12.
 */
public class LocalExtractionHelperTest {

    private ExtractionHelper extractionHelper;
    private TopicCenter topicCenter;
    private static final String TOPIC_NAME_1 = "test_topic1";
    static int capability;
    private CargoDeposition deposition;

    private void depositCargoes(Topic topic, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            deposition.write(new Cargo(topic, topic.incrementAndGetId(), ("message"+i).getBytes()));
        }
    }

    @BeforeClass
    public static void classInit(){
        capability = TopicFileFormat.Offset.CAPABILITY;
    }

    @Before
    public void init(){
        topicCenter = LocalTopicCenter.getInstance();
        PublicMethods.initFields();

        extractionHelper = LocalExtractionHelper.getInstance();
        LocalExtractionHelper.TEST_InitInstance();

        deposition = new LocalDepositionNonSingleton();
        deposition.start();

        topicCenter.register(TOPIC_NAME_1);
    }

//    @Test
    public void fileName() throws InterruptedException {
        Topic topic1 = topicCenter.getTopic(TOPIC_NAME_1);
        deposition.addDepositionListener((topic, cargoId) -> {
            if (cargoId == capability/2 ){
                assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 0),extractionHelper.fileName(topic1, capability-1 ));
                assertEquals(null,extractionHelper.fileName(topic1, capability));
            }else if(cargoId == capability){
                assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 1),extractionHelper.fileName(topic1, capability));
            }
        });

        depositCargoes(topicCenter.getTopic(TOPIC_NAME_1), capability+1);
    }

    @Test
    public void startIndex() throws InterruptedException {
        assertEquals(0, ExtractionHelper.startIndex(12));
        assertEquals(capability, ExtractionHelper.startIndex(capability));
        assertEquals(capability, ExtractionHelper.nextStartIndex(13));
        assertEquals(capability, ExtractionHelper.nextStartIndex(0));


        int depositCount = capability+1;

        CountDownLatch latch = new CountDownLatch(depositCount);


        deposition.addDepositionListener((topic, cargoId) -> {
            PublicMethods.notified(topic, latch, depositCount);
        });

        Topic topic1 = topicCenter.getTopic(TOPIC_NAME_1);

        depositCargoes(topic1, depositCount);

        latch.await(20000, TimeUnit.MICROSECONDS);

        assertEquals(0, extractionHelper.startIndex(topic1, 0));

        System.out.println("============\nTest Finish\n============");
    }


    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME_1);
        PublicMethods.cleanupDeposition(deposition);
    }

}
