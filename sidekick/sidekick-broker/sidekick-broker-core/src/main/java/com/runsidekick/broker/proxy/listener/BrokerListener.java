package com.runsidekick.broker.proxy.listener;

import com.runsidekick.broker.proxy.ChannelInfo;
import org.json.JSONObject;

/**
 * @author serkan.ozal
 */
public interface BrokerListener {

    default void onHandleAppMessage(ChannelInfo channelInfo, JSONObject message) {
    }

    default void onHandleClientMessage(ChannelInfo channelInfo, JSONObject message) {
    }

    default void onHandleApiMessage(ChannelInfo channelInfo, JSONObject message) {
    }

}
