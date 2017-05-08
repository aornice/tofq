package xyz.aornice.tofq.network.netty;

import io.netty.channel.Channel;
import xyz.aornice.tofq.network.AsyncCallback;
import xyz.aornice.tofq.network.Server;
import xyz.aornice.tofq.network.command.Command;

/**
 * tofq's server implementation
 * Created by drfish on 07/05/2017.
 */
public class TofqNettyServer implements Server {
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public int port() {
        return 0;
    }

    @Override
    public Command invokeSync(Channel channel, Command request, long timeoutMillis) {
        return null;
    }

    @Override
    public void invokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback) {

    }

    @Override
    public void invokeOneway(Channel channel, Command request, long timeoutMillis) {

    }
}
