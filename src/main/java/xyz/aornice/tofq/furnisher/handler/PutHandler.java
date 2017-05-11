package xyz.aornice.tofq.furnisher.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.DepositionListener;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.furnisher.message.Message;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;
import xyz.aornice.tofq.furnisher.message.Operation;
import xyz.aornice.tofq.furnisher.message.payload.Put;
import xyz.aornice.tofq.furnisher.message.payload.PutResponseBuilder;
import xyz.aornice.tofq.furnisher.util.support.ArraySortedMap;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;


public class PutHandler extends MessageInboundHandler<Put> implements DepositionListener {

    private static final Logger logger = LogManager.getLogger(PutHandler.class);

    private static final MessageBuilder msgBuilder = new MessageBuilder();
    private static final PutResponseBuilder putRespBuilder = new PutResponseBuilder();

    private final TopicCenter topicCenter;
    private final CargoDeposition deposition;
    private final ArraySortedMap idSeqMap;

    private ChannelHandlerContext ctx;

    private Topic topicCache;


    public PutHandler() {
        this(LocalDeposition.getInstance(), LocalTopicCenter.getInstance());
    }

    public PutHandler(CargoDeposition deposition, TopicCenter topicCenter) {
        this.deposition = deposition;
        this.topicCenter = topicCenter;
        idSeqMap = new ArraySortedMap();
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
            if (topicCache == null) {
                topicCenter.register(msg.getTopic());
                topicCache = topicCenter.getTopic(msg.getTopic());
            }
            deposition.addDepositionListener(topicCache, this);
        }
        final Topic topic = topicCache;
        final Cargo cargo = new Cargo(topic, topic.incrementAndGetId(), msg.getData());
        idSeqMap.add(cargo.getId(), msg.getSeq());
        deposition.write(cargo);
    }

    @Override
    public void notifyDeposition(Topic topic, long cargoId) {
        if (topic != topicCache) return;
        final int seq = idSeqMap.findLEAndClear(cargoId);
        if (seq == -1) return;

        Message msg = msgBuilder.build(
                Operation.PUT_RESP,
                putRespBuilder.build(topic.getName(), seq)
        );
        ctx.executor().submit(() -> ctx.writeAndFlush(msg));
    }
}
