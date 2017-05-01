package xyz.aornice.tofq.depostion;


import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoDeposition {

    /**
     * thread-safe
     * The method will return immediately without guarantee the cargo has been deposited.
     * The deposition success information could getFileContent by adding listener to
     * {@link CargoDeposition#addDepositionListener}
     * @param cargo - the {@link Cargo} need to be deposited
     */
    void write(Cargo cargo);

    /**
     * thread-safe
     * Add {@link DepositionListener} to {@link CargoDeposition}. CargoDeposition will notify
     * the listener when deposition has been done.
     * @param listener - the listener to add
     */
    void addDepositionListener(DepositionListener listener);

    /**
     * Start deposition task
     */
    void start();

    /**
     * Shutdown deposition right now
     */
    void shutdown();

    /**
     * Shutdown deposition gracefully, deposit the cargoes have already been
     * write to CargoDeposition
     */
    void shutdownGracefully();
}
