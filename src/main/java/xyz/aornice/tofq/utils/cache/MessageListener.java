package xyz.aornice.tofq.utils.cache;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

/**
 * Created by shen on 2017/4/21.
 */
public interface MessageListener {
    /**
     * when new msg added, the cache should update.
     * @param cargo
     * @param offset
     */
    void messageAdded(Cargo cargo, long offset);
}
