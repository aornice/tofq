package extraction;

import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;
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
    int capability;

    @Before
    public void init(){
        extractionHelper = LocalExtractionHelper.getInstance();
        topicCenter = LocalTopicCenter.getInstance();
        capability = TopicFileFormat.Offset.CAPABILITY;
    }

    @Test
    public void fileName(){

        assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 0),extractionHelper.fileName(TOPIC_NAME_1, capability-1 ));
        assertEquals(topicCenter.iThFile(TOPIC_NAME_1, 1),extractionHelper.fileName(TOPIC_NAME_1, capability));
//        assertEquals(null,extractionHelper.fileName(TOPIC_NAME_1, capability*2));
    }

    @Test
    public void startIndex(){
        assertEquals(0, extractionHelper.startIndex(12));
        assertEquals(capability, extractionHelper.startIndex(capability));
        assertEquals(capability, extractionHelper.nextStartIndex(13));
        assertEquals(capability, extractionHelper.nextStartIndex(0));
        assertEquals(0, extractionHelper.startIndex(TOPIC_NAME_1, topicCenter.topicOldestFile(TOPIC_NAME_1)));
        System.out.println(topicCenter.iThFile(TOPIC_NAME_1, 1));

//        assertEquals(capability, extractionHelper.startIndex(TOPIC_NAME_1, topicCenter.iThFile(TOPIC_NAME_1,1)));
    }

}
