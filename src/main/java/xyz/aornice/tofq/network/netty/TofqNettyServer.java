package xyz.aornice.tofq.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
    /**
     * bootstrap serverChannel for server
     */
    private final ServerBootstrap serverBootstrap;
    /**
     * main reactor in netty reactor pattern
     */
    private final EventLoopGroup bossGroup;
    /**
     * sub reactor in netty reactor pattern
     */
    private final EventLoopGroup workerGroup;
    /**
     * server config
     */
    private final TofqNettyServerConfig serverConfig;
    /**
     * public thread pool to deal with callback methods
     */
    private final ExecutorService publicExecutor;
    /**
     * the port server is listening
     */
    private int port;


    public TofqNettyServer(TofqNettyServerConfig serverConfig) {
        super(serverConfig.getServerAsyncSemaphoreValue(), serverConfig.getServerOnewaySemaphoreValue());
        this.serverBootstrap = new ServerBootstrap();
        this.serverConfig = serverConfig;

        int callbackExecutorCount = serverConfig.getServerCallbackExecutorCount();
        if (callbackExecutorCount <= 0) {
            callbackExecutorCount = 4;
        }
        this.publicExecutor = Executors.newFixedThreadPool(callbackExecutorCount);

        // there is just one port needs to listen, so main reactor only needs one thread
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(serverConfig.getServerSelectorCount());
    }

    @Override
    public void start() {
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
                                codecFactory.getEncoder(),
                                codecFactory.getDecoder(),
                                new TofqSeverHandler());
                    }
                });

        try {
            // wait until finish binding the channel to specific port
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

    /**
     * server handler to deal with inbound command messages' I/O process
     */
    class TofqSeverHandler extends SimpleChannelInboundHandler<Command> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }

}
