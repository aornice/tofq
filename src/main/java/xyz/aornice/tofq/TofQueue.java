package xyz.aornice.tofq;

import xyz.aornice.tofq.impl.FurnisherData;
import xyz.aornice.tofq.utils.CargoIterator;

import java.nio.file.Path;

/**
 * Created by drfish on 09/04/2017.
 */
public interface TofQueue {

    boolean offer(FurnisherData furnisherData);

    CargoIterator elements(Topic topic);

    /**
     * @return the path of persistent files
     */
    Path getPath();

}
