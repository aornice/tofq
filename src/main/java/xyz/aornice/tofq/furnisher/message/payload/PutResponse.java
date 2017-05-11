package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.channel.ChannelOutboundInvoker;
import xyz.aornice.tofq.furnisher.util.Recyclable;

public interface PutResponse extends Payload {

    String getTopic();

    int getSeq();

}
