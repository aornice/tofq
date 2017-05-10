package xyz.aornice.tofq.network;

/**
 * {@link AsyncCallback} is the callback method for asynchronous call.
 * Created by drfish on 07/05/2017.
 */
public interface AsyncCallback {
    /**
     * once an asynchronous call has been finished, what needs to be done next can implement in this method
     *
     * @param responseFuture response of the asynchronous call
     */
    void onComplete(ResponseFuture responseFuture);
}
