package xyz.aornice.tofq.depostion.support;

/**
 * Created by robin on 22/04/2017.
 */
public class IllegalDepositionStateException extends RuntimeException {

    public IllegalDepositionStateException() {
    }

    public IllegalDepositionStateException(String str) {
        super(str);
    }
}
