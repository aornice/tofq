package xyz.aornice.tofq.network.exception;

/**
 * timeout exception in network
 * Created by drfish on 08/05/2017.
 */
public class NetworkTimeoutException extends NetworkException {
    public NetworkTimeoutException(String message) {
        super(message);
    }

    public NetworkTimeoutException(String address, long timeoutMillis) {
        this(address, timeoutMillis, null);
    }

    public NetworkTimeoutException(String address, long timeoutMillis, Throwable cause) {
        super("wait response on the channel " + address + " timeout, " + timeoutMillis + "(ms)", cause);
    }
}
