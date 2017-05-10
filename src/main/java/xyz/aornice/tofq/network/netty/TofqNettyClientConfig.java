package xyz.aornice.tofq.network.netty;

/**
 * Tofq client config
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyClientConfig {
    /**
     * client executor thread count
     */
    private int clientWorkerCount = 4;
    /**
     * client callback executors' count
     */
    private int clientCallbackExecutorCount = Runtime.getRuntime().availableProcessors();
    /**
     * client concurrent oneway requests' count
     */
    private int clientOnewaySemaphoreValue = 65535;
    /**
     * client concurrent asynchronous requests' count
     */
    private int clientAsyncSemaphoreValue = 65535;
    /**
     * client connection timeout in milliseconds
     */
    private int connectTimeoutMillis = 3000;
    /**
     * client socket send buffer size
     */
    private int clientSocketSndBufSize = 1024;
    /**
     * client socket receive buffer size
     */
    private int clientSocketRcvBufSize = 1024;

    public int getClientWorkerCount() {
        return clientWorkerCount;
    }

    public void setClientWorkerCount(int clientWorkerCount) {
        this.clientWorkerCount = clientWorkerCount;
    }

    public int getClientCallbackExecutorCount() {
        return clientCallbackExecutorCount;
    }

    public void setClientCallbackExecutorCount(int clientCallbackExecutorCount) {
        this.clientCallbackExecutorCount = clientCallbackExecutorCount;
    }

    public int getClientOnewaySemaphoreValue() {
        return clientOnewaySemaphoreValue;
    }

    public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
        this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
    }

    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }

    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }
}
