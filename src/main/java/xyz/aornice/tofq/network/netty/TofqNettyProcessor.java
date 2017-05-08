package xyz.aornice.tofq.network.netty;

import io.netty.channel.ChannelHandlerContext;
import xyz.aornice.tofq.network.command.Command;

/**
 * Created by drfish on 08/05/2017.
 */
public interface TofqNettyProcessor {
    Command processRequest(ChannelHandlerContext ctx, Command request);
}
