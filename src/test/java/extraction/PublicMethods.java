package extraction;

import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

/**
 * Created by shen on 2017/5/6.
 */
public class PublicMethods {
    public static void initFields(){
        LocalTopicCenter.TEST_InitFields();
        LocalHarbour.TEST_InitFields();
    }

    public static void startDeposition(CargoDeposition deposition){
        deposition.start();
    }

    public static void cleanupDeposition(CargoDeposition deposition) throws InterruptedException {
        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }
    }
}
