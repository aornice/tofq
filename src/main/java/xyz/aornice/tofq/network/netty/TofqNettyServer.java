package xyz.aornice.tofq.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.AsyncCallback;
import xyz.aornice.tofq.network.Server;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.exception.NetworkSendRequestException;
import xyz.aornice.tofq.network.exception.NetworkTimeoutException;
import xyz.aornice.tofq.network.exception.NetworkTooManyRequestsException;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * tofq's server implementation
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyServer extends TofqNettyInvokeAbstract implements Server {
    private static final Logger logger = LoggerFactory.getLogger(TofqNettyServer.class);
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final TofqNettyServerConfig serverConfig;
    private final ExecutorService publicExecutor;
    private int port;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;


    public TofqNettyServer(TofqNettyServerConfig serverConfig) {
        super(serverConfig.getServerAsyncSemaphoreValue(), serverConfig.getServerOnewaySemaphoreValue());
        this.serverBootstrap = new ServerBootstrap();
        this.serverConfig = serverConfig;


        int callbackExecutorCount = serverConfig.getServerCallbackExecutorCount();
        if (callbackExecutorCount <= 0) {
            callbackExecutorCount = 4;
        }
        this.publicExecutor = Executors.newFixedThreadPool(callbackExecutorCount);

        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(serverConfig.getServerSelectorCount());
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(serverConfig.getServerWorkerCount());
        TofqNettyCodecFactory codecFactory = new TofqNettyCodecFactory(getCodec());
        this.serverBootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, serverConfig.getServerSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, serverConfig.getServerSocketRcvBufSize())
                .localAddress(serverConfig.getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup,
                                codecFactory.getEncoder(),
                                codecFactory.getDecoder(),
                                new TofqSeverHandler());
                    }
                });
        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            logger.debug("tofq sever start");
            logger.debug("tofq server config: {}", serverConfig);
            InetSocketAddress address = (InetSocketAddress) sync.channel().localAddress();
            this.port = address.getPort();
        } catch (InterruptedException e) {
            logger.error("serverBootstrap.bind().sync interrupted exception {}", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.bossGroup.shutdownGracefully();
            this.workerGroup.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
            if (this.publicExecutor != null) {
                this.publicExecutor.shutdown();
            }
        } catch (Exception e) {
            logger.error("TofqNettyServer shutdown exception, {}", e);
        }
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public Command invokeSync(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkTimeoutException, NetworkSendRequestException {
        return this.doInvokeSync(channel, request, timeoutMillis);
    }

    @Override
    public void invokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback) throws InterruptedException, NetworkTooManyRequestsException, NetworkSendRequestException {
        this.doInvokeAsync(channel, request, timeoutMillis, asyncCallback);
    }

    @Override
    public void invokeOneway(Channel channel, Command request, long timeoutMillis) throws InterruptedException, NetworkSendRequestException, NetworkTooManyRequestsException, NetworkTimeoutException {
        this.doInvokeOneway(channel, request, timeoutMillis);
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }

    class TofqSeverHandler extends SimpleChannelInboundHandler<Command> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }

}
