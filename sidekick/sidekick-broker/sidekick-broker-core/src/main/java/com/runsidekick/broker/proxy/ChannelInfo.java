package com.runsidekick.broker.proxy;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author serkan.ozal
 */
@Data
public class ChannelInfo {

    final Channel channel;
    final String connectionId;
    final long connectTime;

    volatile ChannelType channelType;
    volatile String sessionId;
    volatile String sessionGroupId;
    volatile String userId;
    volatile String accountId;
    volatile String workspaceId;
    volatile String ip;
    volatile long latestActiveTime;
    volatile boolean authenticated;
    volatile Map<String, String> tags;
    volatile ChannelAuthTimeoutHandler channelAuthTimeoutHandler;
    volatile ScheduledFuture authFuture;
    volatile ChannelMetadata channelMetadata;

    final AtomicBoolean handshakeStarted = new AtomicBoolean(false);

    public ChannelInfo(Channel channel, String connectionId, long connectTime) {
        this.channel = channel;
        this.connectionId = connectionId;
        this.connectTime = connectTime;
    }

    public void cancelTimeoutHandler() {
        if (channelAuthTimeoutHandler != null && channelAuthTimeoutHandler.cancel()) {
            ScheduledFuture authFtr = authFuture;
            if (authFtr != null) {
                authFtr.cancel(true);
            }
        }
        authFuture = null;
    }
}
