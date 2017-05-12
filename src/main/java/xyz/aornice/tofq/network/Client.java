package xyz.aornice.tofq.network;

import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkConnectException;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;
import xyz.aornice.tofq.network.netty.TofqNettyProcessor;

import java.util.concurrent.ExecutorService;

/**
 * client interface
 * Created by drfish on 07/05/2017.
 */
public interface Client extends State {
    /**
     * register processor for specific type of request
     *
     * @param requestCode request type code
     * @param processor   processor to deal with request
     * @param executor    executors to execute processors' request
     */
    void registerProcessor(int requestCode, TofqNettyProcessor processor, ExecutorService executor);

    /**
     * send a synchronous request to an ip address
     *
     * @param address       ip address
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @return response command
     * @throws InterruptedException
     * @throws NetworkConnectException
     * @throws NetworkTimeoutException
     * @throws NetworkSendRequestException
     */
    Command invokeSync(String address, Command request, long timeoutMillis) throws InterruptedException,
            NetworkConnectException, NetworkTimeoutException, NetworkSendRequestException;

    /**
     * send a asynchronous request to an ip address
     *
     * @param address       ip address
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @param asyncCallback asynchronous callback methed
     * @throws InterruptedException
     * @throws NetworkConnectException
     * @throws NetworkSendRequestException
     * @throws NetworkTooManyRequestsException
     */
    void invokeAsync(String address, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws
            InterruptedException, NetworkConnectException, NetworkSendRequestException, NetworkTooManyRequestsException;

    /**
     * send a oneway request to an ip address
     *
     * @param address       ip address
     * @param request       request command
     * @param timeoutMillis timeout in milliseconds
     * @throws InterruptedException
     * @throws NetworkTooManyRequestsException
     * @throws NetworkTimeoutException
     * @throws NetworkSendRequestException
     */
    void invokeOneway(String address, Command request, long timeoutMillis) throws InterruptedException,
            NetworkTooManyRequestsException, NetworkTimeoutException, NetworkSendRequestException;
}