package com.runsidekick.broker.proxy.listener.impl;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.listener.BrokerListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author serkan.ozal
 */
public class CompositeBrokerListener implements BrokerListener {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final List<BrokerListener> listeners = new CopyOnWriteArrayList<>();

    public void registerListener(BrokerListener listener) {
        listeners.add(listener);
    }

    public void deregisterListener(BrokerListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public void onHandleAppMessage(ChannelInfo channelInfo, JSONObject message) {
        for (BrokerListener listener : listeners) {
            try {
                listener.onHandleAppMessage(channelInfo, message);
            } catch (Throwable t) {
                logger.error(
                        String.format("Error occurred while listening 'onHandleAppMessage' by listener %s", listener),
                        t);
            }
        }
    }

    @Override
    public void onHandleClientMessage(ChannelInfo channelInfo, JSONObject message) {
        for (BrokerListener listener : listeners) {
            try {
                listener.onHandleClientMessage(channelInfo, message);
            } catch (Throwable t) {
                logger.error(
                        String.format("Error occurred while listening 'onHandleClientMessage' by listener %s",
                                listener),
                        t);
            }
        }
    }

    @Override
    public void onHandleApiMessage(ChannelInfo channelInfo, JSONObject message) {
        for (BrokerListener listener : listeners) {
            try {
                listener.onHandleApiMessage(channelInfo, message);
            } catch (Throwable t) {
                logger.error(
                        String.format("Error occurred while listening 'onHandleApiMessage' by listener %s",
                                listener),
                        t);
            }
        }
    }

}
