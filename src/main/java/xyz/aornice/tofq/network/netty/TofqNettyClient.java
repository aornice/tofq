package xyz.aornice.tofq.network.netty;

import xyz.aornice.tofq.network.AsyncCallback;
import xyz.aornice.tofq.network.Client;
import xyz.aornice.tofq.network.command.Command;

/**
 * tofq's client implementation
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyClient implements Client{
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public Command invokeSync(String addr, Command request, long timeoutMillis) {
        return null;
    }

    @Override
    public void invokeAsync(String addr, Command request, long timeoutMillis, AsyncCallback asyncCallback) {

    }

    @Override
    public void invokeOneway(String addr, Command request, long timeoutMillis) {

    }
}
