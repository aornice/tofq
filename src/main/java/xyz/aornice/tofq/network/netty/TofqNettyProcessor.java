package xyz.aornice.tofq.network.netty;

import io.netty.channel.ChannelHandlerContext;
import xyz.aornice.tofq.network.command.Command;

/**
 * processor to deal with received messages, it can be used in both clients and servers
 * Created by drfish on 08/05/2017.
 */
public interface TofqNettyProcessor {
    /**
     * process the received message
     *
     * @param ctx     channel handler context receive the request
     * @param request received request message
     * @return
     */
    Command processRequest(ChannelHandlerContext ctx, Command request);
}
