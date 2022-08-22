package com.runsidekick.broker.proxy.listener.impl;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ChannelType;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.proxy.listener.AuthenticationListener;
import com.runsidekick.broker.proxy.listener.SessionCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class AuthenticationListenerImpl implements AuthenticationListener {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationListenerImpl.class);

    private static final AttributeKey<String> CLOSE_REASON_ATTRIBUTE_KEY = AttributeKey.valueOf("CloseReason");

    private static final String CLOSE_REASON_SESSION_LIMIT_EXCEEDED = "SessionLimitExceeded";

    @Autowired
    private SessionCallback sessionCallback;

    @Autowired
    private SessionService sessionService;

    public void onAuthFail(ChannelInfo channelInfo, String reason) {
        Channel channel = channelInfo.getChannel();
        InetSocketAddress address = (InetSocketAddress) channel.localAddress();
        LOGGER.info(
                "Channel (@ {}: {}) authentication failed, so closing",
                address.getPort(), channel.remoteAddress());

        channel.attr(CLOSE_REASON_ATTRIBUTE_KEY).set(reason);
        channelInfo.cancelTimeoutHandler();
        channel.close();
    }

    public boolean onAuthSuccess(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                                 WebSocketServerHandshaker wsHandshaker, FullHttpRequest req) {
        Channel channel = channelInfo.getChannel();
        InetSocketAddress address = (InetSocketAddress) channel.localAddress();

        LOGGER.info(
                "Channel (@ {}: {}) authenticated",
                address.getPort(), channel.remoteAddress());

        channelInfo.setAuthenticated(true);
        channelInfo.cancelTimeoutHandler();

        ChannelInfo existingChannelInfo = addSession(channelInfo);

        if (existingChannelInfo != null && existingChannelInfo.getChannelType().equals(ChannelType.APP)) {
            LOGGER.info(
                    "There is already attached idle channel in the session, " +
                            "so closing existing one (@ {}: {})",
                    address.getPort(), existingChannelInfo.getChannel().remoteAddress());
            existingChannelInfo.getChannel().attr(CLOSE_REASON_ATTRIBUTE_KEY).
                    set(CLOSE_REASON_SESSION_LIMIT_EXCEEDED);
            sendCloseMessage(
                    existingChannelInfo,
                    WebSocketCloseStatus.NORMAL_CLOSURE,
                    "There is already attached idle channel in the session, so closing existing one");
            existingChannelInfo.getChannel().close();
        }

        wsHandshaker.handshake(channel, req);

        return true;
    }


    void logWriteMessage(ChannelInfo channelInfo, Object msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Channel ({}) write",
                    channelInfo.getChannel().remoteAddress());
            if (msg instanceof TextWebSocketFrame) {
                LOGGER.debug("Message: {}", ((TextWebSocketFrame) msg).text());
            }
        }
    }


    void sendCloseMessage(ChannelInfo channelInfo,
                          WebSocketCloseStatus webSocketCloseStatus,
                          String closeMessage) {
        CloseWebSocketFrame message = new CloseWebSocketFrame(webSocketCloseStatus, closeMessage);
        logWriteMessage(channelInfo, message);
        channelInfo.getChannel().writeAndFlush(message);
    }

    ChannelInfo addSession(ChannelInfo channelInfo) {
        return sessionService.addSession(channelInfo, sessionCallback);
    }
}

