package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.CargoDeposition;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.TofQueue;

import java.nio.file.Path;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalTofQueue implements TofQueue {
    public static final String TEST_PATH = "queue";
    public static final String SUFFIX = ".tof";

    private CargoDeposition furnisher;
    private CargoExtraction receiptor;

    public LocalTofQueue(String name) {
        furnisher = new LocalDeposition(TEST_PATH, name);
        receiptor = new LocalExtraction(TEST_PATH, name);
    }

    public LocalTofQueue() {
        this("temp");
    }


    @Override
    public boolean offer(String message) {
        furnisher.write(message);
        return true;
    }

    @Override
    public String elements() {
        String messages = receiptor.readAll();
        return messages;
    }

    @Override
    public Path getPath() {
        return null;
    }

}
