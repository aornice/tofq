package xyz.aornice.tofq.network.exception;

/**
 * exception used when a client fails to send a request
 * Created by drfish on 08/05/2017.
 */
public class NetworkSendRequestException extends NetworkException {
    public NetworkSendRequestException(String address) {
        this(address, null);
    }

    public NetworkSendRequestException(String address, Throwable cause) {
        super("send request to " + address + " failed", cause);
    }
}
