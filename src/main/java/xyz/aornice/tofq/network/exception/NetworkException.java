package xyz.aornice.tofq.network.exception;

/**
 * super class of all network exceptions
 * Created by drfish on 08/05/2017.
 */
public class NetworkException extends Exception {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
