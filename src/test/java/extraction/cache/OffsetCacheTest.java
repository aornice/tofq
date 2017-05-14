package extraction.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.cache.OffsetCache;
import xyz.aornice.tofq.utils.cache.impl.FileOffsetCache;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shen on 2017/5/14.
 */
public class OffsetCacheTest {
    private OffsetCache offsetCache;
    private final String TOPIC_NAME = "test_topic";
    private TopicCenter topicCenter;

    @Before
    public void init(){
        offsetCache = new FileOffsetCache();
        topicCenter = LocalTopicCenter.getInstance();
        LocalTopicCenter.TEST_InitFields();
        topicCenter.register(TOPIC_NAME);
    }

    @Test
    public void test1(){
        Topic testTopic = topicCenter.getTopic(TOPIC_NAME);

        List<Long> offsets = new ArrayList<Long>(){
            {
                for (long i = 0; i < 1024; i++) {
                    add(i);
                }
            }
        };

        Assert.assertEquals(null, offsetCache.getCache(testTopic, 0));
        offsetCache.putCache(testTopic, 0, offsets);

        Assert.assertArrayEquals(offsets.toArray(), offsetCache.getCache(testTopic, 0).toArray());

        offsetCache.clearCache();

        Assert.assertEquals(null, offsetCache.getCache(testTopic, 0));

        for (int i = 0; i< offsetCache.getCapacity(); i++){
            offsetCache.putCache(testTopic, i, offsets);
        }

        Assert.assertArrayEquals(offsets.toArray(), offsetCache.getCache(testTopic, 0).toArray());

        offsetCache.putCache(testTopic, offsetCache.getCapacity(), offsets);

        Assert.assertArrayEquals(offsets.toArray(), offsetCache.getCache(testTopic, 0).toArray());
        Assert.assertEquals(null, offsetCache.getCache(testTopic, 1));

    }

}
