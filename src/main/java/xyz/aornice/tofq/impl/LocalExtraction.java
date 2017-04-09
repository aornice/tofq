package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.CargoExtraction;

import java.io.*;

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
    public String readAll() {
        File file = new File(path + "/" + name + LocalTofQueue.SUFFIX);
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            line = br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    @Override
    public String read() {
        return null;
    }
}
