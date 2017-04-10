package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.CargoDeposition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalDeposition implements CargoDeposition {
    private String path;
    private String name;

    public LocalDeposition(final String path, final String name) {
        this.path = path;
        this.name = name;
        checkDir(path);
    }

    private void checkDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    @Override
    public void write(Cargo cargo) {
    }

    @Override
    public void write(Cargo[] cargo) {

    }
}
