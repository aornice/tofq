package utils;

import xyz.aornice.tofq.utils.TopicCenter;

import java.util.Set;

/**
 * Created by cat on 2017/4/10.
 */
public class TopicCenterTest {
    public static void main(String[] args){
        Set<String> topics = TopicCenter.getTopics();
        for(String topic : topics){
            System.out.println(topic);
        }

        System.out.println();

        TopicCenter.register("topic4");

        topics = TopicCenter.getTopics();
        for(String topic: topics){
            System.out.println(topic);
        }

        System.out.println();


        System.out.println(TopicCenter.getPath("topic4"));
    }
}
