package extraction.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.cache.ContentCache;
import xyz.aornice.tofq.utils.cache.impl.FileContentCache;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shen on 2017/5/14.
 */
public class ContentCacheTest {

    private ContentCache contentCache;
    private final String TOPIC_NAME = "test_topic";
    private TopicCenter topicCenter;

    @Before
    public void init(){
        contentCache = new FileContentCache();
        topicCenter = LocalTopicCenter.getInstance();
        LocalTopicCenter.TEST_InitFields();
        topicCenter.register(TOPIC_NAME);
    }

    @Test
    public void test1(){
        Topic testTopic = topicCenter.getTopic(TOPIC_NAME);

        List<byte[]> contents = new ArrayList<byte[]>(){
            {add("123456".getBytes());}
        };

        Assert.assertEquals(null,contentCache.getCache(testTopic, 0));
        contentCache.putCache(testTopic, 0, contents);

        Assert.assertArrayEquals("123456".getBytes(), contentCache.getCache(testTopic, 0).get(0));

        contentCache.clearCache();

        Assert.assertEquals(null,contentCache.getCache(testTopic, 0));

        for (int i=0; i< contentCache.getCapacity(); i++){
            contentCache.putCache(testTopic, i, contents);
        }

        Assert.assertArrayEquals("123456".getBytes(), contentCache.getCache(testTopic, 0).get(0));

        contentCache.putCache(testTopic, contentCache.getCapacity(), contents);

        Assert.assertArrayEquals("123456".getBytes(), contentCache.getCache(testTopic, 0).get(0));
        Assert.assertEquals(null, contentCache.getCache(testTopic, 1));

    }


}
