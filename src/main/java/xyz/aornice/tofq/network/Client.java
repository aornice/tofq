package xyz.aornice.tofq.network;

import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkConnectException;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;

/**
 * client interface
 * Created by drfish on 07/05/2017.
 */
public interface Client extends State {
    Command invokeSync(String address, Command request, long timeoutMillis) throws InterruptedException, NetworkConnectException, NetworkTimeoutException, NetworkSendRequestException;

    void invokeAsync(String address, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws InterruptedException, NetworkConnectException, NetworkSendRequestException, NetworkTooManyRequestsException;

    void invokeOneway(String address, Command request, long timeoutMillis) throws InterruptedException, NetworkTooManyRequestsException, NetworkTimeoutException, NetworkSendRequestException;
}