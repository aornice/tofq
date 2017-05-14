package xyz.aornice.tofq.network.netty;

/**
 * Tofq server config
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyServerConfig {
    /**
     * port which is listened by server
     */
    private int port = 31515;
    /**
     * server concurrent oneway requests' count
     */
    private int serverOnewaySemaphoreValue = 256;
    /**
     * server concurrent asynchronous requests' count
     */
    private int serverAsyncSemaphoreValue = 64;
    /**
     * server callback executors' count
     */
    private int serverCallbackExecutorCount = 4;
    /**
     * server select pool's threads count in reactor model
     */
    private int serverSelectorCount = 3;
    /**
     * server worker executors count
     */
    private int serverWorkerCount = 8;
    /**
     * server socket send buffer size
     */
    private int serverSocketSndBufSize = 1024;
    /**
     * server socket receive buffer size
     */
    private int serverSocketRcvBufSize = 1024;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }

    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }

    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }

    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }

    public int getServerCallbackExecutorCount() {
        return serverCallbackExecutorCount;
    }

    public void setServerCallbackExecutorCount(int serverCallbackExecutorCount) {
        this.serverCallbackExecutorCount = serverCallbackExecutorCount;
    }

    public int getServerSelectorCount() {
        return serverSelectorCount;
    }

    public void setServerSelectorCount(int serverSelectorCount) {
        this.serverSelectorCount = serverSelectorCount;
    }

    public int getServerWorkerCount() {
        return serverWorkerCount;
    }

    public void setServerWorkerCount(int serverWorkerCount) {
        this.serverWorkerCount = serverWorkerCount;
    }

    public int getServerSocketSndBufSize() {
        return serverSocketSndBufSize;
    }

    public void setServerSocketSndBufSize(int serverSocketSndBufSize) {
        this.serverSocketSndBufSize = serverSocketSndBufSize;
    }

    public int getServerSocketRcvBufSize() {
        return serverSocketRcvBufSize;
    }

    public void setServerSocketRcvBufSize(int serverSocketRcvBufSize) {
        this.serverSocketRcvBufSize = serverSocketRcvBufSize;
    }

    @Override
    public String toString() {
        return "RemotingServerConfig [port=" + port + ", serverWorkerCount=" + serverWorkerCount
                + ", serverCallbackExecutorCount=" + serverCallbackExecutorCount + ", serverSelectorCount="
                + serverSelectorCount + ", serverOnewaySemaphoreValue=" + serverOnewaySemaphoreValue
                + ", serverAsyncSemaphoreValue=" + serverAsyncSemaphoreValue + "]";
    }
}
