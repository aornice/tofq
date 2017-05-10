package xyz.aornice.tofq.furnisher.handler;

import io.netty.channel.ChannelHandlerContext;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.furnisher.message.payload.Put;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;


public class PutHandler extends MessageInboundHandler<Put> implements DepositionListener{

    final private TopicCenter topicCenter;
    final private CargoDeposition deposition;

    private ChannelHandlerContext ctx;

    private Topic topicCache;

    public PutHandler() {
        this(LocalDeposition.getInstance(), LocalTopicCenter.getInstance());
    }

    public PutHandler(CargoDeposition deposition, TopicCenter topicCenter) {
        this.deposition = deposition;
        this.topicCenter = topicCenter;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.ctx = ctx;
    }

    @Override
    public void messageReceived(ChannelHandlerContext cxt, Put msg)
            throws Exception {
        if (topicCache == null || !topicCache.getName().equals(msg.getTopic())) {
            topicCache = topicCenter.getTopic(msg.getTopic());
            deposition.addDepositionListener(topicCache, this);
        }
        final Topic topic = topicCache;
        final Cargo cargo = new Cargo(topic, topic.incrementAndGetId(), msg.getData());
        deposition.write(cargo);
    }

    @Override
    public void notifyDeposition(Topic topic, long cargoId) {
        if (topicCache != topic) return;
    }

}
