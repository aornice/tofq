package xyz.aornice.tofq;

import xyz.aornice.tofq.impl.Cargo;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoExtraction {
    Cargo[] readAll();

    Cargo read(String topic, long id);

    /**
     * return cargos in topic of a range
     * @param topic topic name
     * @param from  start index of the cargo
     * @param to    end index of the cargo
     * @return      return cargos, null if the topic not exists
     */
    Cargo[] read(String topic, long from, long to);
}
