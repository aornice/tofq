package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.TofQueue;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.CargoIterator;

import java.nio.file.Path;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalTofQueue implements TofQueue {
    public static final String TEST_PATH = "queue";
    public static final String SUFFIX = ".tof";

    private CargoDeposition deposition;
    private CargoExtraction extraction;
    private Harbour harbour;

    public LocalTofQueue() {
        harbour = new LocalHarbour(TEST_PATH);
        deposition = LocalDeposition.getInstance();
        extraction = new LocalExtraction(harbour);
    }

    @Override
    public boolean offer(FurnisherData furnisherData) {
        return true;
    }

    @Override
    public CargoIterator elements(Topic topic) {
        CargoIterator cargos = extraction.readAll(topic);
        return cargos;
    }

    @Override
    public Path getPath() {
        return null;
    }

}
