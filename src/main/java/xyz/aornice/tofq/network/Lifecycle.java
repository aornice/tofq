package xyz.aornice.tofq.network;

/**
 * {@link Lifecycle} maintains the state of a client or server.
 * Created by drfish on 07/05/2017.
 */
public interface Lifecycle {
    /**
     * start a C/S
     */
    void start();

    /**
     * shutdown a C/S
     */
    void shutdown();
}
