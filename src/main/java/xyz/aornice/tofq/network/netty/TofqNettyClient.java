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

import java.net.InetSocketAddress;
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
        // clear channel map
    }

    @Override
    public Command invokeSync(String address, Command request, long timeoutMillis) {
        return null;
    }

    @Override
    public void invokeAsync(String address, Command request, long timeoutMillis, AsyncCallback asyncCallback) {

    }

    @Override
    public void invokeOneway(String address, Command request, long timeoutMillis) {

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
            logger.warn("try to get channel map lock timeout");
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

    }

    private InetSocketAddress string2InetSocketAddress(String address) {
        String[] splits = address.split(";");
        InetSocketAddress socketAddress = new InetSocketAddress(splits[0], Integer.valueOf(splits[1]));
        return socketAddress;
    }


    @Override
    public ExecutorService getCallbackExecutor() {
        return null;
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
