package com.runsidekick.broker.proxy.listener;

import com.runsidekick.broker.proxy.ChannelInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

public interface AuthenticationListener {

    void onAuthFail(ChannelInfo channelInfo, String reason);

    boolean onAuthSuccess(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                          WebSocketServerHandshaker wsHandshaker, FullHttpRequest req);
}
