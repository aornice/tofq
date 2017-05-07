package xyz.aornice.tofq.network;

/**
 * {@link State} maintains the state of a client or server.
 * Created by drfish on 07/05/2017.
 */
public interface State {
    void start();

    void shutdown();
}
