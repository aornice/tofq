package xyz.aornice.tofq;

import xyz.aornice.tofq.utils.CargoIterator;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoExtraction {
    CargoIterator readAll(Topic topic);

    Cargo read(Topic topic, long id);

    Cargo[] recentNCargos(Topic topic, int nCargos);

    CargoIterator recentNCargosIterator(Topic topic, long nCargos);

    /**
     * return cargos in topic of a range
     *
     * @param topic topic name
     * @param from  start index of the cargo, included
     * @param to    end index of the cargo, not included
     * @return return cargos, null if the topic not exists
     */
    Cargo[] read(Topic topic, long from, long to);

    CargoIterator readIterator(Topic topic, long from, long to);

}
