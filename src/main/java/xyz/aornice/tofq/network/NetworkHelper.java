package xyz.aornice.tofq.network;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by drfish on 09/05/2017.
 */
public class NetworkHelper {
    private static final Logger logger = LoggerFactory.getLogger(NetworkHelper.class);

    public static void closeChannel(Channel channel) {
        channel.close().addListener((channelFuture) -> {
            logger.info("close channel to {}, result: {}", channel.remoteAddress(), channelFuture.isSuccess());
        });
    }
}
