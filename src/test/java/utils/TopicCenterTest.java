package utils;

import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.Set;

/**
 * Created by cat on 2017/4/10.
 */
public class TopicCenterTest {
    public static void main(String[] args){
        TopicCenter topicCenter = LocalTopicCenter.newInstance();
        Set<String> topics = topicCenter.getTopics();
        for(String topic : topics){
            System.out.println(topic);
        }

        System.out.println();

        topicCenter.register("topic4");

        topics = topicCenter.getTopics();
        for(String topic: topics){
            System.out.println(topic);
        }

        System.out.println();


        System.out.println(topicCenter.getPath("topic4"));
    }
}
