package xyz.aornice.tofq.network;

import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.util.SemaphoreReleaseOnlyOnce;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link ResponseFuture} encapsulates the response of a request
 * Created by drfish on 07/05/2017.
 */
public class ResponseFuture {
    /**
     * match response with request
     */
    private final int opaque;
    /**
     * timeout in millisecond
     */
    private final long timeoutMillis;
    /**
     * callback for asynchronous method
     */
    private final AsyncCallback asyncCallback;
    /**
     * begin time of the request
     */
    private final long beginTimestamp = System.currentTimeMillis();
    /**
     * synchronization used to wait for the related response
     */
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    /**
     * util to ensure the semaphore release only once
     */
    private final SemaphoreReleaseOnlyOnce once;
    /**
     * ensure the callback method is executed only once
     */
    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    /**
     * store received response command
     */
    private volatile Command responseCommand;
    /**
     * if the request related with response has been sent successfully
     */
    private volatile boolean sendRequestOK = true;
    /**
     * exception within the asynchronous request
     */
    private volatile Throwable cause;

    public ResponseFuture(int opaque, long timeoutMillis, AsyncCallback asyncCallback, SemaphoreReleaseOnlyOnce once) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.asyncCallback = asyncCallback;
        this.once = once;
    }

    public void executeAsyncCallback() {
        if (asyncCallback != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                asyncCallback.onComplete(this);
            }
        }
    }

    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }

    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    public Command waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public void putResponse(final Command responseCommand) {
        this.responseCommand = responseCommand;
        this.countDownLatch.countDown();
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public AsyncCallback getAsyncCallback() {
        return asyncCallback;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Command getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(Command responseCommand) {
        this.responseCommand = responseCommand;
    }

    public int getOpaque() {
        return opaque;
    }

    @Override
    public String toString() {
        return "ResponseFuture [responseCommand=" + responseCommand + ", sendRequestOK=" + sendRequestOK
                + ", cause=" + cause + ", opaque=" + opaque + ", timeoutMillis=" + timeoutMillis
                + ", asyncCallback=" + asyncCallback + ", beginTimestamp=" + beginTimestamp
                + ", countDownLatch=" + countDownLatch + "]";
    }
}
