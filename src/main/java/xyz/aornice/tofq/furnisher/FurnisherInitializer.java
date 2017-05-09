package xyz.aornice.tofq.furnisher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import xyz.aornice.tofq.furnisher.codec.FurnisherMessageDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32FrameDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32LengthFieldPrepender;
import xyz.aornice.tofq.furnisher.handler.PutHandler;


public class FurnisherInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new FurnisherVarint32FrameDecoder());
        pipeline.addLast(new FurnisherVarint32LengthFieldPrepender());
        pipeline.addLast(new FurnisherMessageDecoder());
        pipeline.addLast(new PutHandler());
    }
}
