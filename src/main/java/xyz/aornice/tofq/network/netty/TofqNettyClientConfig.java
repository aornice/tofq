package xyz.aornice.tofq.network.netty;

/**
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyClientConfig {
    private int clientWorkerCount = 4;
    private int clientCallbackExecutorCount = Runtime.getRuntime().availableProcessors();
    private int clientOnewaySemaphoreValue = 65535;
    private int clientAsyncSemaphoreValue = 65535;
    private int connectTimeoutMillis = 3000;
    private int clientSocketSndBufSize = 65535;
    private int clientSocketRcvBufSize = 65535;

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
