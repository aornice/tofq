package xyz.aornice.tofq.network.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
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
    private final TofqNettyClientConfig clientConfig;
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroupWorker;

    private final ExecutorService publicExecutor;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private final ConcurrentMap<String, ChannelWrapper> channelMap = new ConcurrentHashMap<>();
    private final Lock channelMapLock = new ReentrantLock();
    private static final int CHANNEL_MAP_LOCK_TIMEOUT_MILLIS = 2000;

    public TofqNettyClient(TofqNettyClientConfig clientConfig) {
        super(clientConfig.getClientAsyncSemaphoreValue(), clientConfig.getClientOnewaySemaphoreValue());
        this.clientConfig = clientConfig;

        int callbackExecutorCount = clientConfig.getClientCallbackExecutorCount();
        if (callbackExecutorCount <= 0) {
            callbackExecutorCount = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(callbackExecutorCount);
        this.eventLoopGroupWorker = new NioEventLoopGroup(1);
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(clientConfig.getClientWorkerCount());
        TofqNettyCodecFactory tofqNettyCodecFactory = new TofqNettyCodecFactory(getCodec());
        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
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
            this.eventLoopGroupWorker.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("client shutdown exception {}", e);
        }
        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                logger.error("server shutdown exception {}", e);
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

    private Channel getOrCreateChannel(String address) throws InterruptedException {
        ChannelWrapper channelWrapper = this.channelMap.get(address);
        if (channelWrapper != null && channelWrapper.isActive()) {
            return channelWrapper.getChannel();
        }
        if (this.channelMapLock.tryLock(CHANNEL_MAP_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                channelWrapper = this.channelMap.get(address);
                if (channelWrapper != null) {
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

    private void closeChannel(String address, Channel channel) {
        if (channel == null) {
            return;
        }
        String remoteAddress = address == null ? getAddressFromChannel(channel) : address;
        try {
            if (this.channelMapLock.tryLock(CHANNEL_MAP_LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeOld = true;
                    ChannelWrapper prevChannelWrapper = this.channelMap.get(remoteAddress);
                    if (prevChannelWrapper == null) {
                        removeOld = false;
                    } else if (prevChannelWrapper.getChannel() != channel) {
                        removeOld = false;
                    }
                    if (removeOld) {
                        this.channelMap.remove(remoteAddress);
                    }
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

    class TofqClientHandler extends SimpleChannelInboundHandler<Command> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }

    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

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
