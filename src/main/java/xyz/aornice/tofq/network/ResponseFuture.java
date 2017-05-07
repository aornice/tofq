package xyz.aornice.tofq.network;

/**
 * {@link ResponseFuture} encapsulates the response of an asynchronous request
 * Created by drfish on 07/05/2017.
 */
public class ResponseFuture {
    private final int opaque;


    public ResponseFuture(int opaque, long timeoutMillis, AsyncCallback asyncCallback) {
        this.opaque = opaque;
    }
}
