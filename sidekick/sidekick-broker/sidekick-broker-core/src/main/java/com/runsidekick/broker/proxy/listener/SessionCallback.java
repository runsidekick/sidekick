package com.runsidekick.broker.proxy.listener;

import com.runsidekick.broker.proxy.ChannelInfo;

public interface SessionCallback {

    default void onApplicationSessionAdd(ChannelInfo channelInfo) {
    }

    default void onClientSessionAdd(ChannelInfo channelInfo) {
    }

    default void onApiSessionAdd(ChannelInfo channelInfo) {
    }

    default void onApplicationSessionRemove(ChannelInfo channelInfo) {
    }

    default void onClientSessionRemove(ChannelInfo channelInfo) {
    }

    default void onApiSessionRemove(ChannelInfo channelInfo) {
    }
}
