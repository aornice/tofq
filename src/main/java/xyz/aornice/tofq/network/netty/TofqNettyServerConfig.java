package xyz.aornice.tofq.network.netty;

/**
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyServerConfig {
    private int port = 31515;
    private int serverOnewaySemaphoreValue = 256;
    private int serverAsyncSemaphoreValue = 64;
    private int serverCallbackExecutorCount = 4;
    private int serverSelectorCount = 3;
    private int serverWorkerCount = 8;

    private int serverSocketSndBufSize = 65535;
    private int serverSocketRcvBufSize = 65535;

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
