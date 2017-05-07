package extraction;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.concurrent.CountDownLatch;

/**
 * Created by shen on 2017/5/6.
 */
public class PublicMethods {
    public static void initFields(){
        LocalTopicCenter.TEST_InitFields();
        LocalHarbour.TEST_InitFields();
    }

    public static void cleanupDeposition(CargoDeposition deposition) throws InterruptedException {
        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }
    }

    public static void notified(Topic topic, CountDownLatch latch, long latchTotal){
        long currentCounted = latchTotal - latch.getCount();
        long maxStoredId = topic.getMaxStoredId();
        for (long i=0; i<=maxStoredId-currentCounted; i++){
            latch.countDown();
        }
    }
}
