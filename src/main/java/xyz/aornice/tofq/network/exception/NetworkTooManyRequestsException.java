package xyz.aornice.tofq.network.exception;

/**
 * exception when there are too many clients trying to send requests
 * Created by drfish on 08/05/2017.
 */
public class NetworkTooManyRequestsException extends NetworkException {
    public NetworkTooManyRequestsException(String message) {
        super(message);
    }
}
