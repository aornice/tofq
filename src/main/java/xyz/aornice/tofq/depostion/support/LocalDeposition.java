package xyz.aornice.tofq.depostion.support;

import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.util.SortedQueue;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalDeposition implements CargoDeposition {

    private ConcurrentMap<Topic, SortedQueue<Cargo>> topicMap;

    private static final int BATCH_DEPOSITION_SIZE = 300;
    private static final long DEPOSTION_INTERVAL = 5;


    private LocalDeposition() {
    }

    public static CargoDeposition getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void write(Cargo cargo) {
        topicMap.get(cargo.getTopic()).put(cargo);
        if (topicMap.size() == BATCH_DEPOSITION_SIZE) notifyDeposition();
    }

    @Override
    public void addDepositionListener(DepositionListener listener) {
    }

    private void notifyDeposition() {}

    private static class Singleton {
        static LocalDeposition INSTANCE = new LocalDeposition();
    }
}
