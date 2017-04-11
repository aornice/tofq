package xyz.aornice.tofq;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoExtraction {
    Cargo[] readAll();

    Cargo read(String topic, long id);

    Cargo[] read(String topic, long from, long to);
}
