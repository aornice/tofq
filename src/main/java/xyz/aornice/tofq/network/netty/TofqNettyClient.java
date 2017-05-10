package xyz.aornice.tofq.network.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.AsyncCallback;
import xyz.aornice.tofq.network.Client;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkConnectException;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * tofq's client implementation
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyClient extends TofqNettyInvokeAbstract implements Client {
    private static final Logger logger = LoggerFactory.getLogger(TofqNettyClient.class);
    /**
     * client config
     */
    private final TofqNettyClientConfig clientConfig;
    /**
     * bootstrap channels to use for clients
     */
    private final Bootstrap bootstrap = new Bootstrap();
    /**
     * handle all the events with the to-be-created Channel
     */
    private final EventLoopGroup workerGroup;
    /**
     * public thread pool to deal with callback methods
     */
    private final ExecutorService publicExecutor;
    /**
     * cache ip address and its channel in a map
     */
    private final ConcurrentMap<String, ChannelWrapper> channelMap = new ConcurrentHashMap<>();
    /**
     * lock for {@link #channelMap} modification
     */
    private final Lock channelMapLock = new ReentrantLock();
    /**
     * maximum time to wait for the {@link #channelMapLock}
     */
    private static final int CHANNEL_MAP_LOCK_TIMEOUT_MILLIS = 2000;

    public TofqNettyClient(TofqNettyClientConfig clientConfig) {
        super(clientConfig.getClientAsyncSemaphoreValue(), clientConfig.getClientOnewaySemaphoreValue());
        this.clientConfig = clientConfig;

        int callbackExecutorCount = clientConfig.getClientCallbackExecutorCount();
        if (callbackExecutorCount <= 0) {
            callbackExecutorCount = 4;
        }
        this.publicExecutor = Executors.newFixedThreadPool(callbackExecutorCount);
        this.workerGroup = new NioEventLoopGroup(1);
    }

    @Override
    public void start() {
        TofqNettyCodecFactory tofqNettyCodecFactory = new TofqNettyCodecFactory(getCodec());
        this.bootstrap.group(this.workerGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                tofqNettyCodecFactory.getEncoder(),
                                tofqNettyCodecFactory.getDecoder(),
                                new TofqClientHandler());
                    }
                });
    }

    @Override
    public void shutdown() {
        try {
            // clear channel map
            for (Map.Entry<String, ChannelWrapper> entry : this.channelMap.entrySet()) {
                closeChannel(entry.getKey(), entry.getValue().getChannel());
            }
            this.channelMap.clear();

            this.workerGroup.shutdownGracefully();
        } catch (Exception e) {
            logger.error("client shutdown exception {}", e);
        }
        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                logger.error("public callback executors shutdown exception {}", e);
            }
        }
    }

    @Override
    public Command invokeSync(String address, Command request, long timeoutMillis) throws InterruptedException, NetworkConnectException, NetworkTimeoutException, NetworkSendRequestException {
        Channel channel = getOrCreateChannel(address);
        if (channel != null && channel.isActive()) {
            try {
                Command response = this.doInvokeSync(channel, request, timeoutMillis);
                return response;
            } catch (NetworkTimeoutException e) {
                // TODO whether should close channel when response timeout
                logger.warn("client invokeSync: wait response time out for {}", address);
                throw e;
            } catch (NetworkSendRequestException e) {
                logger.warn("client invokeSync: send request exception, close {}", address);
                this.closeChannel(address, channel);
                throw e;
            }
        } else {
            closeChannel(address, channel);
            throw new NetworkConnectException(address);
        }
    }

    @Override
    public void invokeAsync(String address, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws InterruptedException, NetworkConnectException, NetworkSendRequestException, NetworkTooManyRequestsException {
        Channel channel = getOrCreateChannel(address);
        if (channel != null && channel.isActive()) {
            try {
                this.doInvokeAsync(channel, request, timeoutMillis, asyncCallback);
            } catch (NetworkSendRequestException e) {
                logger.warn("client invokeAsync: send request exception, close {}", address);
                closeChannel(address, channel);
                throw e;
            }
        } else {
            closeChannel(address, channel);
            throw new NetworkConnectException(address);
        }

    }

    @Override
    public void invokeOneway(String address, Command request, long timeoutMillis) throws InterruptedException, NetworkTooManyRequestsException, NetworkTimeoutException, NetworkSendRequestException {
        Channel channel = getOrCreateChannel(address);
        if (channel != null && channel.isActive()) {
            try {
                this.doInvokeOneway(channel, request, timeoutMillis);
            } catch (NetworkSendRequestException e) {
                logger.warn("client invokeOneway: send request exception, close {}", address);
                closeChannel(address, channel);
                throw e;
            }
        }
    }

    /**
     * get or create if there is not related channel with the ip address
     *
     * @param address ip address
     * @return related channel
     * @throws InterruptedException
     */
    private Channel getOrCreateChannel(String address) throws InterruptedException {
        // get channel from cache map
        ChannelWrapper channelWrapper = this.channelMap.get(address);
        if (channelWrapper != null && channelWrapper.isActive()) {
            return channelWrapper.getChannel();
        }
        // create a new connection with ip address to get channel and update cache map
        if (this.channelMapLock.tryLock(CHANNEL_MAP_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                // double check
                channelWrapper = this.channelMap.get(address);
                if (channelWrapper != null) {
                    // if the channel is active or hasn't been completed, there is no need to create new connection
                    if (channelWrapper.isActive()) {
                        return channelWrapper.getChannel();
                    } else if (!channelWrapper.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelMap.remove(address);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }
                //create new connection and update cache
                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(string2InetSocketAddress(address));
                    channelWrapper = new ChannelWrapper(channelFuture);
                    this.channelMap.put(address, channelWrapper);
                }
            } catch (Exception e) {
                logger.error("create channel {} exception {}", address, e);
            } finally {
                this.channelMapLock.unlock();
            }
        } else {
            logger.warn("create channel: try to get channel map lock timeout");
        }
        // wait for the channel to complete its task or connecting
        if (channelWrapper != null) {
            ChannelFuture channelFuture = channelWrapper.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.clientConfig.getConnectTimeoutMillis())) {
                if (channelWrapper.isActive()) {
                    return channelWrapper.getChannel();
                } else {
                    logger.warn("create channel to {} failed {}", address, channelFuture.cause());
                }
            } else {
                logger.warn("create channel to {} timeout", address);
            }
        }
        return null;
    }

    /**
     * close the channel with an ip address, must represent channel since ip address can be got from channel
     *
     * @param address ip address, can be {@code null}
     * @param channel channel, cannot be {@code null}
     */
    private void closeChannel(String address, Channel channel) {
        if (channel == null) {
            return;
        }
        // get ip address if it is not presented
        String remoteAddress = address == null ? getAddressFromChannel(channel) : address;
        try {
            if (this.channelMapLock.tryLock(CHANNEL_MAP_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeOld = true;
                    // check if there is need to update cache map
                    ChannelWrapper prevChannelWrapper = this.channelMap.get(remoteAddress);
                    if (prevChannelWrapper == null) {
                        removeOld = false;
                    } else if (prevChannelWrapper.getChannel() != channel) {
                        removeOld = false;
                    }
                    if (removeOld) {
                        this.channelMap.remove(remoteAddress);
                    }
                    // close the channel anyway
                    channel.close();
                } catch (Exception e) {
                    logger.error("close channel of {} failed {}", remoteAddress, e);
                } finally {
                    this.channelMapLock.unlock();
                }
            } else {
                logger.warn("close channel: try to get channel map lock timeout");
            }
        } catch (InterruptedException e) {
            logger.error("close channel: exception {}", e);
        }

    }

    /**
     * get the remote address where the channel is connected to
     *
     * @param channel I/O channel
     * @return remote address related to the channel
     */
    private String getAddressFromChannel(Channel channel) {
        if (channel == null) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        String address = remote != null ? remote.toString() : "";
        if (address.length() > 0) {
            int index = address.lastIndexOf("/");
            if (index >= 0) {
                return address.substring(index + 1);
            }
        }
        return address;
    }

    private InetSocketAddress string2InetSocketAddress(String address) {
        String[] splits = address.split(":");
        InetSocketAddress socketAddress = new InetSocketAddress(splits[0], Integer.valueOf(splits[1]));
        return socketAddress;
    }


    @Override
    public ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }

    /**
     * client handler to deal with inbound command messages' I/O process
     */
    class TofqClientHandler extends SimpleChannelInboundHandler<Command> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }

    /**
     * {@link ChannelWrapper} is a wrapper class for {@link ChannelFuture} which represents the result of an asynchronous channel I/O operation
     * This class's aim is just to simplify usage of state check of the channel, like method {@link #isActive()}
     */
    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }


        /**
         * whether the channelFuture connects with a channel and the channel is active(connected)
         *
         * @return return {@code true} if so, else {@code false}
         */
        public boolean isActive() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        public ChannelFuture getChannelFuture() {
            return this.channelFuture;
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }
    }

}
