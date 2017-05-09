package xyz.aornice.tofq.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.AsyncCallback;
import xyz.aornice.tofq.network.ResponseFuture;
import xyz.aornice.tofq.network.codec.Codec;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.command.protocol.ResponseCode;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;
import xyz.aornice.tofq.util.SemaphoreReleaseOnlyOnce;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * {@link TofqNettyInvokeAbstract} is an abstraction of invoke process, it has basic method of different type
 * of invoke process, both C/S base on invoke process
 * Created by drfish on 07/05/2017.
 */
public abstract class TofqNettyInvokeAbstract {
    private static final Logger logger = LoggerFactory.getLogger(TofqNettyInvokeAbstract.class);
    /**
     * semaphore used to control concurrent asynchronous requests
     */
    protected final Semaphore asyncSemaphore;
    /**
     * semaphore used to control concurrent oneway requests
     */
    protected final Semaphore onewaySemaphore;
    /**
     * asynchronous request buffer
     * key is the opaque field of a request
     * value is the ResponseFuture of the related request
     */
    protected final ConcurrentMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);
    /**
     * each kind of requests can register their own processor, if a request code doesn't register its processor,
     * it will use {@link #defaultProcessor} by default
     * each processor has a thread pool to process requests
     */
    protected final Map<Integer, Pair<TofqNettyProcessor, ExecutorService>> processorTable = new HashMap<>();
    /**
     * default request processor
     */
    protected Pair<TofqNettyProcessor, ExecutorService> defaultProcessor;


    public TofqNettyInvokeAbstract(int asyncCount, int onewayCount) {
        this.asyncSemaphore = new Semaphore(asyncCount, true);
        this.onewaySemaphore = new Semaphore(onewayCount, true);
    }

    /**
     * get callback executor to execute callback methods
     *
     * @return
     */
    public abstract ExecutorService getCallbackExecutor();

    /**
     * process a C/S received message
     *
     * @param ctx
     * @param message
     */
    public void processMessageReceived(ChannelHandlerContext ctx, Command message) {
        if (message != null) {
            switch (message.getType()) {
                case REQUEST:
                    processRequestCommand(ctx, message);
                    break;
                case RESPONSE:
                    processResponseCommand(ctx, message);
                    break;
            }
        }

    }

    /**
     * process a request message
     *
     * @param ctx
     * @param request
     */
    public void processRequestCommand(ChannelHandlerContext ctx, Command request) {
        int opaque = request.getOpaque();
        final Pair<TofqNettyProcessor, ExecutorService> pair = this.processorTable.get(request.getCode()) == null ? defaultProcessor : this.processorTable.get(request.getCode());
        if (pair != null) {
            Runnable runnable = () -> {
                try {
                    Command response = pair.getKey().processRequest(ctx, request);
                    if (!request.isOneway()) {
                        if (response != null) {
                            response.setOpaque(opaque);

                            try {
                                ctx.writeAndFlush(response);
                            } catch (Throwable cause) {
                                logger.error("finish processing request: {}, but response {} failed", request, response);
                            }
                        }
                    }
                } catch (Throwable cause) {
                    logger.error("process request {} exception", request, cause);
                    // send an error response to failed request
                    if (!request.isOneway()) {
                        Command response = Command.createResponseCommand(ResponseCode.PROCESS_ERROR);
                        response.setOpaque(opaque);
                        ctx.writeAndFlush(response);
                    }
                }
            };
            try {
                pair.getValue().submit(runnable);
            } catch (RejectedExecutionException e) {
                // thread pool reject to execute
                logger.warn("{}, thread pool busy, RejectedExecutionException {}, request code: {}", ctx.channel().remoteAddress(), pair.getValue(), request.getCode());
                if (!request.isOneway()) {
                    final Command response = Command.createResponseCommand(ResponseCode.SYSTEM_BUSY);
                    response.setOpaque(request.getOpaque());
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            Command response = Command.createResponseCommand(ResponseCode.REQUESTED_CODE_NOT_SUPPORTED);
            response.setOpaque(opaque);
            ctx.writeAndFlush(response);
            logger.error("{} request type {} not supported", ctx.channel().remoteAddress(), request.getCode());
        }
    }

    /**
     * process a response message
     *
     * @param ctx
     * @param response
     */
    public void processResponseCommand(ChannelHandlerContext ctx, Command response) {
        int opaque = response.getOpaque();
        ResponseFuture responseFuture = responseTable.get(opaque);
        if (responseFuture != null) {
            responseFuture.setResponseCommand(response);
            responseFuture.release();
            responseTable.remove(opaque);

            if (responseFuture.getAsyncCallback() != null) {
                executeInvokeCallback(responseFuture);
            } else {
                responseFuture.putResponse(response);
            }
        } else {
            logger.warn("receive response {} from {}, but not match any request", response.toString(), ctx.channel().remoteAddress().toString());
        }
    }

    /**
     * kernel implementation of the synchronous invoke method
     *
     * @param channel       channel to deal with the request
     * @param request       request command
     * @param timeoutMillis timeout in millisecond
     * @return
     * @throws InterruptedException
     * @throws NetworkTimeoutException
     */
    public Command doInvokeSync(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkTimeoutException, NetworkSendRequestException {
        int opaque = request.getOpaque();
        try {
            ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            channel.writeAndFlush(request).addListener((channelFuture) -> {
                if (channelFuture.isSuccess()) {
                    responseFuture.setSendRequestOK(true);
                    return;
                } else {
                    responseFuture.setSendRequestOK(false);
                }
                responseTable.remove(opaque);
                responseFuture.setCause(channelFuture.cause());
                responseFuture.putResponse(null);
                logger.warn("send a request command to channel {} failed.", channel.remoteAddress());
            });
            Command responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (responseCommand == null) {
                if (responseFuture.isSendRequestOK()) {
                    throw new NetworkTimeoutException(channel.remoteAddress().toString(), timeoutMillis, responseFuture.getCause());
                } else {
                    throw new NetworkSendRequestException(channel.remoteAddress().toString(), responseFuture.getCause());
                }
            }
            return responseCommand;
        } finally {
            this.responseTable.remove(opaque);
        }
    }

    /**
     * kernel implementation of the asynchronous invoke method
     *
     * @param channel       channel to deal with the request
     * @param request       request command
     * @param timeoutMillis timeout in millisecond
     * @param asyncCallback callback of the asynchronous request
     * @throws InterruptedException
     * @throws NetworkSendRequestException
     * @throws NetworkTooManyRequestsException
     */
    public void doInvokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws InterruptedException, NetworkSendRequestException, NetworkTooManyRequestsException {
        int opaque = request.getOpaque();
        boolean acquired = this.asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        // check if there are too many asynchronous requests at the same time
        if (acquired) {
            SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.asyncSemaphore);
            ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, asyncCallback, once);
            this.responseTable.put(opaque, responseFuture);
            try {
                channel.writeAndFlush(request).addListener((channelFuture) -> {
                    // try to invoke synchronously
                    if (channelFuture.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }
                    responseTable.remove(opaque);
                    responseFuture.putResponse(null);
                    // if invoke synchronously failed, invoke the callback method
                    executeInvokeCallback(responseFuture);
                    logger.warn("send a request command to {} failed", channel.remoteAddress());
                });
            } catch (Exception e) {
                throw new NetworkSendRequestException(channel.remoteAddress().toString(), e);
            } finally {
                once.release();
            }
        } else {
            String message = String.format("invokeAsyncImpl tryAcquire semaphore timeout, {}ms, waiting thread nums: {} semaphoreAsyncValue: {}",
                    timeoutMillis,
                    this.asyncSemaphore.getQueueLength(),
                    this.asyncSemaphore.availablePermits()
            );
            throw new NetworkTooManyRequestsException(message);
        }
    }

    /**
     * execute the callback method in callback executor, if there is no callback executor, run the callback method in current thread
     *
     * @param responseFuture response future
     */
    private void executeInvokeCallback(ResponseFuture responseFuture) {
        boolean runInThisThread = false;
        ExecutorService executor = this.getCallbackExecutor();
        // try to run callback in callback executor
        if (executor != null) {
            try {
                executor.submit(() -> {
                    try {
                        responseFuture.executeAsyncCallback();
                    } catch (Throwable cause) {
                        logger.warn("execute callback in callback executor, throw {}", cause);
                    }
                });
            } catch (Exception e) {
                runInThisThread = true;
                logger.warn("execute callback in callback executor, the task cannot be scheduled");
            }
        } else {
            runInThisThread = true;
        }
        // if there is no callback executor or get exception in callback process, try to execute callback in current thread
        if (runInThisThread) {
            try {
                responseFuture.executeAsyncCallback();
            } catch (Throwable cause) {
                logger.warn("execute callback in current thread, throw {}", cause);
            }
        }
    }

    /**
     * kernel implementation of one way invoke method
     *
     * @param channel       channel to deal with the request
     * @param request       request command
     * @param timeoutMillis timeout in millisecond
     * @throws InterruptedException
     * @throws NetworkSendRequestException
     * @throws NetworkTooManyRequestsException
     * @throws NetworkTimeoutException
     */
    public void doInvokeOneway(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkSendRequestException, NetworkTooManyRequestsException, NetworkTimeoutException {
        request.setOneway(true);
        boolean acquired = this.onewaySemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        // check if there are too many one way requests at the same time
        if (acquired) {
            SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.onewaySemaphore);
            try {
                channel.writeAndFlush(request).addListener((channelFuture) -> {
                    once.release();
                    if (!channelFuture.isSuccess()) {
                        logger.warn("send a request to channel {} failed", channel.remoteAddress());
                    }
                });
            } catch (Exception e) {
                once.release();
                throw new NetworkSendRequestException(channel.remoteAddress().toString(), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new NetworkTooManyRequestsException("doInvokeOneway invoke too fast");
            } else {
                String message = String.format("invokeAsyncImpl tryAcquire semaphore timeout, {}ms, waiting thread nums: {} semaphoreAsyncValue: {}",
                        timeoutMillis,
                        this.asyncSemaphore.getQueueLength(),
                        this.asyncSemaphore.availablePermits()
                );
                throw new NetworkTimeoutException(message);
            }
        }
    }

    protected Codec getCodec() {
        // TODO choose proper codec
        return null;
    }
}
