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

    @Test
    public void fileName() throws InterruptedException {
        deposition.addDepositionListener((topic, cargoId) -> {
            if (cargoId == capability/2 ){
                assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 0),extractionHelper.fileName(TOPIC_NAME_1, capability-1 ));
                assertEquals(null,extractionHelper.fileName(TOPIC_NAME_1, capability));
            }else if(cargoId == capability){
                assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 1),extractionHelper.fileName(TOPIC_NAME_1, capability));
            }
        });

        depositCargoes(topicCenter.getTopic(TOPIC_NAME_1), capability+1);
    }

    @Test
    public void startIndex() throws InterruptedException {
        assertEquals(0, extractionHelper.startIndex(12));
        assertEquals(capability, extractionHelper.startIndex(capability));
        assertEquals(capability, extractionHelper.nextStartIndex(13));
        assertEquals(capability, extractionHelper.nextStartIndex(0));


        deposition.addDepositionListener((topic, cargoId) -> {
            if (cargoId == capability ){
                assertEquals(0, extractionHelper.startIndex(TOPIC_NAME_1, topicCenter.topicOldestFile(TOPIC_NAME_1)));
            }
        });
        depositCargoes(topicCenter.getTopic(TOPIC_NAME_1), capability+1);
    }


    @After
    public void cleanup() throws InterruptedException {
        topicCenter.remove(TOPIC_NAME_1);
        PublicMethods.cleanupDeposition(deposition);
    }

}
