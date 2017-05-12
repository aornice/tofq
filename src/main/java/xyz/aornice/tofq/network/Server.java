package xyz.aornice.tofq.network;

import io.netty.channel.Channel;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;
import xyz.aornice.tofq.network.netty.TofqNettyProcessor;

import java.util.concurrent.ExecutorService;

/**
 * server interface
 * Created by drfish on 07/05/2017.
 */
public interface Server extends State {
    /**
     * get the port which is being listened by server
     *
     * @return listening port
     */
    int port();

    /**
     * register processor for specific type of request
     *
     * @param requestCode request type code
     * @param processor   processor to deal with request
     * @param executor    executors to execute processors' request
     */
    void registerProcessor(int requestCode, TofqNettyProcessor processor, ExecutorService executor);

    /**
     * send a synchronous request to a channel
     *
     * @param channel       a nexus of the network socket with client
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @return response command
     * @throws InterruptedException
     * @throws NetworkTimeoutException
     * @throws NetworkSendRequestException
     */
    Command invokeSync(Channel channel, Command request, long timeoutMillis) throws InterruptedException,
            NetworkTimeoutException, NetworkSendRequestException;

    /**
     * send a asynchronous request to a channel
     *
     * @param channel       a nexus of the network socket with client
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @param asyncCallback asynchronous callback method
     * @throws InterruptedException
     * @throws NetworkTooManyRequestsException
     * @throws NetworkSendRequestException
     */
    void invokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws
            InterruptedException, NetworkTooManyRequestsException, NetworkSendRequestException;

    /**
     * send a oneway request to a channel
     *
     * @param channel       a nexus of the network socket with client
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @throws InterruptedException
     * @throws NetworkSendRequestException
     * @throws NetworkTooManyRequestsException
     * @throws NetworkTimeoutException
     */
    void invokeOneway(Channel channel, Command request, long timeoutMillis) throws InterruptedException,
            NetworkSendRequestException, NetworkTooManyRequestsException, NetworkTimeoutException;
}
