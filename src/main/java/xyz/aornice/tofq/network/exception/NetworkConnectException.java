package xyz.aornice.tofq.network.exception;

/**
 * exception for client fails to connect with sever
 * Created by drfish on 10/05/2017.
 */
public class NetworkConnectException extends NetworkException {
    public NetworkConnectException(String address) {
        super(address, null);
    }

    public NetworkConnectException(String address, Throwable cause) {
        super("connect to " + address + " failed", cause);
    }
}
