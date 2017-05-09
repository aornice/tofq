package xyz.aornice.tofq.network;

import io.netty.channel.Channel;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;

/**
 * server interface
 * Created by drfish on 07/05/2017.
 */
public interface Server extends State {
    int port();

    Command invokeSync(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkTimeoutException, NetworkSendRequestException;

    void invokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws InterruptedException, NetworkTooManyRequestsException, NetworkSendRequestException;

    void invokeOneway(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkSendRequestException, NetworkTooManyRequestsException, NetworkTimeoutException;
}
