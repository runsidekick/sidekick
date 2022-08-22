package com.runsidekick.broker.handler.message;

import com.runsidekick.broker.proxy.ChannelInfo;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author tolgatakir
 */
public interface MessageHandler {
    void handleWebSocketMessage(ChannelInfo channelInfo, WebSocketFrame messageFrame);
}
