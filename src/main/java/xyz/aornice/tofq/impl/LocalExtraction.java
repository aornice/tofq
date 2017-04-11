package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.utils.TopicCenter;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalExtraction implements CargoExtraction {
    private String path;
    private String name;

    public LocalExtraction(final String path, final String name) {
        this.path = path;
        this.name = name;
    }

    @Override
    public Cargo[] readAll() {
        return null;
    }

    @Override
    public Cargo read(String topic, long id) {
        return null;
    }

    @Override
    public Cargo[] read(String topic, long from, long to) {
        if (! TopicCenter.getTopics().contains(topic)){
            return null;
        }

        return null;
    }

}
