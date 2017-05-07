package xyz.aornice.tofq.network;

/**
 * {@link AsyncCallback} is the callback method for asynchronous call.
 * Created by drfish on 07/05/2017.
 */
public interface AsyncCallback {
    void onComplete(ResponseFuture responseFuture);
}
