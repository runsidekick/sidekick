package com.runsidekick.broker.service;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.listener.AuthenticationListener;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Service
public abstract class AuthenticationService {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    @Autowired
    AuthenticationListener authenticationListener;

    public abstract boolean authenticate(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                                         FullHttpRequest req, HttpHeaders headers);

    protected boolean authFailed(ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req,
                                 String closeMessage, String closeReasonInvalidCredentials) {
        LOGGER.error(closeMessage);
        sendHttpResponse(
                ctx, channelInfo, req,
                new DefaultFullHttpResponse(
                        HTTP_1_1,
                        UNAUTHORIZED,
                        Unpooled.copiedBuffer(closeMessage, CharsetUtil.UTF_8)));
        authenticationListener.onAuthFail(channelInfo, closeReasonInvalidCredentials);
        return false;
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                                  FullHttpRequest req, FullHttpResponse res) {
        logWriteMessage(channelInfo, res);
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != HttpStatus.OK.value()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void logWriteMessage(ChannelInfo channelInfo, Object msg) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Channel ({}) write",
                    channelInfo.getChannel().remoteAddress());
            if (msg instanceof TextWebSocketFrame) {
                LOGGER.debug("Message: {}", ((TextWebSocketFrame) msg).text());
            }
        }
    }


}
