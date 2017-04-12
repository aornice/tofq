package xyz.aornice.tofq;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoExtraction {
    Cargo[] readAll();

    Cargo read(Topic topic, long id);

    /**
     * return cargos in topic of a range
     *
     * @param topic topic name
     * @param from  start index of the cargo, included
     * @param to    end index of the cargo, not included
     * @return      return cargos, null if the topic not exists
     */
    Cargo[] read(Topic topic, long from, long to);
}
