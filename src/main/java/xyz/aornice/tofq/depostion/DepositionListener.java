package xyz.aornice.tofq.depostion;

/**
 * Created by robin on 11/04/2017.
 */
public interface DepositionListener {

    /**
     * When Deposition deposit the cargo, Deposition call DepositionListener's notifyDeposition
     * to info the cargo before (include itself) has been deposited.
     * @param topic - the topic which is notified
     * @param cargoId -the max cargo id that has been deposited
     */
    void notifyDeposition(String topic, long cargoId);
}
