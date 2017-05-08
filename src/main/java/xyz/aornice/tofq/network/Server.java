package xyz.aornice.tofq.network;

import io.netty.channel.Channel;
import xyz.aornice.tofq.network.command.Command;

/**
 * server interface
 * Created by drfish on 07/05/2017.
 */
public interface Server extends State {
    int port();

    Command invokeSync(Channel channel, Command request, long timeoutMillis);

    void invokeAsync(Channel channel, Command request, long timeoutMillis, AsyncCallback asyncCallback);

    void invokeOneway(Channel channel, Command request, long timeoutMillis);
}
