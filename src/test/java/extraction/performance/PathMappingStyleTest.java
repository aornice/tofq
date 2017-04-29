package extraction.performance;

import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by shen on 2017/4/29.
 */
public class PathMappingStyleTest {

    volatile String a = "";


    @Test
    public void differentMappingStyle(){
        Map<String, List<String> > topicPathMap = new HashMap<>();
        Map<String,String> pathMap = new HashMap<>();
        Map<String, List<String> > topicNameMap = new HashMap<>();
        for (int i=0;i<100;i++){
            List<String> list = new ArrayList<>();
            String topicPath = "aaa/aaa/aaa/aa/aaa/aaa/aaa/aaa/"+i;
            for (int j=0; j<10000;j++) {
                list.add(topicPath+"topic_file_name_" + j);
            }

            List<String> nameList = new ArrayList<>();
            for (int j=0; j<10000;j++) {
                nameList.add("topic_file_name_" + j);
            }
            topicNameMap.put("topic" + i, nameList);
            topicPathMap.put("topic" + i, list);
            pathMap.put("topic"+i, topicPath);
        }


        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int k=0;k<100;k++) {
            for (int i = 0; i < 100; i++) {
                a = pathMap.get("topic"+i)+"/"+topicNameMap.get("topic" + i).get(0);
            }
        }

        System.out.println("use concating elapsed "+stopwatch.elapsed(TimeUnit.MILLISECONDS)+" milliseconds");

        stopwatch = Stopwatch.createStarted();
        for (int k=0;k<100;k++) {
            for (int i = 0; i < 100; i++) {
                a = topicPathMap.get("topic"+i).get(0);
            }
        }

        System.out.println("use full path elapsed "+stopwatch.elapsed(TimeUnit.MILLISECONDS)+" milliseconds");


        System.out.println(a);
    }
}
