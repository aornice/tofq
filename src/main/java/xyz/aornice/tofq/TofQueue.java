package xyz.aornice.tofq;

import java.nio.file.Path;

/**
 * Created by drfish on 09/04/2017.
 */
public interface TofQueue {

    boolean offer(String message);

    String elements();

    /**
     * @return the path of persistent files
     */
    Path getPath();

}
