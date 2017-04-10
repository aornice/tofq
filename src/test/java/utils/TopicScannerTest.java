package utils;

import xyz.aornice.tofq.utils.TopicScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by cat on 2017/4/10.
 */
public class TopicScannerTest {
    public static void main(String[] args){
        Set<String> topics = TopicScanner.getTopics();
        for(String topic : topics){
            System.out.println(topic);
        }

        System.out.println();

        TopicScanner.register("topic4");

        topics = TopicScanner.getTopics();
        for(String topic: topics){
            System.out.println(topic);
        }

        System.out.println();


        System.out.println(TopicScanner.getPath("topic4"));
    }
}
