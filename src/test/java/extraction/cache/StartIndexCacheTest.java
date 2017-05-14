package extraction.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.cache.StartIndexCache;
import xyz.aornice.tofq.utils.cache.impl.FileStartIndexCache;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shen on 2017/5/14.
 */
public class StartIndexCacheTest {
    private StartIndexCache startIndexCache;
    private final String TOPIC_NAME = "test_topic";
    private TopicCenter topicCenter;

    @Before
    public void init(){
        startIndexCache = new FileStartIndexCache();
        topicCenter = LocalTopicCenter.getInstance();
        LocalTopicCenter.TEST_InitFields();
        topicCenter.register(TOPIC_NAME);
    }

    @Test
    public void test1(){
        Topic testTopic = topicCenter.getTopic(TOPIC_NAME);

        Assert.assertEquals(null, startIndexCache.getCache(testTopic, 0));
        startIndexCache.putCache(testTopic, 0, new Long(0));

        Assert.assertEquals(0, startIndexCache.getCache(testTopic, 0).intValue());

        startIndexCache.clearCache();

        Assert.assertEquals(null, startIndexCache.getCache(testTopic, 0));

        for (long i = 0; i< startIndexCache.getCapacity(); i++){
            startIndexCache.putCache(testTopic, i, i);
        }

        Assert.assertEquals(0 , startIndexCache.getCache(testTopic, 0).intValue());

        startIndexCache.putCache(testTopic, startIndexCache.getCapacity(), new Long(startIndexCache.getCapacity()));

        Assert.assertEquals(0, startIndexCache.getCache(testTopic, 0).intValue());
        Assert.assertEquals(null, startIndexCache.getCache(testTopic, 1));

    }

}
