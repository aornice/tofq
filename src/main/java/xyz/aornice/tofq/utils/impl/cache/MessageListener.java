package xyz.aornice.tofq.utils.impl.cache;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

/**
 * Created by shen on 2017/4/21.
 */
public interface MessageListener {
    void messageAdded(Cargo cargo, long offset);
}
