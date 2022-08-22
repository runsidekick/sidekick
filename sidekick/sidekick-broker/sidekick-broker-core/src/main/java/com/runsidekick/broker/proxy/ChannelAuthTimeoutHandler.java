package com.runsidekick.broker.proxy;

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * @author serkan.ozal
 */
class ChannelAuthTimeoutHandler implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ChannelAuthTimeoutHandler.class);

    private final Object mutex = new Object();
    private final ChannelInfo channelInfo;
    private boolean cancelled;
    private boolean closed;

    ChannelAuthTimeoutHandler(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    @Override
    public void run() {
        synchronized (mutex) {
            if (!cancelled && !channelInfo.authenticated && !channelInfo.handshakeStarted.get()) {
                Channel channel = channelInfo.channel;
                InetSocketAddress address = (InetSocketAddress) channel.localAddress();
                LOGGER.warn(
                        "Channel closing because of auth timeout on port {} from {}",
                        address.getPort(), channel.remoteAddress());
                channelInfo.channel.attr(Broker.CLOSE_REASON_ATTRIBUTE_KEY).
                        set(Broker.CLOSE_REASON_AUTH_TIMEOUT);
                channelInfo.channel.close();
                closed = true;
            }
        }
    }

    boolean cancel() {
        synchronized (mutex) {
            if (closed) {
                return false;
            } else {
                cancelled = true;
                return true;
            }
        }
    }

}
