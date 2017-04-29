package extraction;

import org.junit.Test;
import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.CargoFileUtil;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import static org.junit.Assert.*;

import java.util.Calendar;

/**
 * Created by shen on 2017/4/28.
 */
public class CargoFileUtilTest {

    private static final String TOPIC_NAME_1 = "topic_test_topic1";

    @Test
    public void dateCompare(){

        TopicCenter topicCenter = LocalTopicCenter.getInstance();
        String fileName = topicCenter.topicNewestFile(TOPIC_NAME_1);
        Calendar calendar = Calendar.getInstance();
        String today = CargoFileUtil.dateStr(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = CargoFileUtil.dateStr(calendar.getTime());
        assertEquals(0, CargoFileUtil.fileCompareDateStr(fileName, today));
        assertEquals(1, CargoFileUtil.fileCompareDateStr(fileName, yesterday));
    }
}
