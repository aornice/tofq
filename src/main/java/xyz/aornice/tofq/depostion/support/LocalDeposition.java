package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;


/**
 * Created by robin on 22/04/2017.
 */
public class LocalDeposition extends AbstractDeposition {

    public static CargoDeposition getInstance() {
        return Singleton.INSTANCE;
    }

    protected static void init(AbstractDeposition deposition) {
        deposition.setTopicCenter(LocalTopicCenter.getInstance());
        deposition.setHarbour(new LocalHarbour());
    }

    private static class Singleton {
        static LocalDeposition INSTANCE = new LocalDeposition();

        static {
            init(INSTANCE);
        }
    }
}
