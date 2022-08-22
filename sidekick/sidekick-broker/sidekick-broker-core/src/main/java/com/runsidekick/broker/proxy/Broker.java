package com.runsidekick.broker.proxy;

import com.runsidekick.broker.handler.message.MessageHandler;
import com.runsidekick.broker.proxy.listener.AuthenticationListener;
import com.runsidekick.broker.proxy.listener.SessionCallback;
import com.runsidekick.broker.service.ApiAuthenticationService;
import com.runsidekick.broker.service.AuthenticationService;
import com.runsidekick.broker.service.SessionService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author serkan.ozal
 */
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class Broker {

    static final AttributeKey<ChannelInfo> CHANNEL_INFO_ATTRIBUTE_KEY =
            AttributeKey.valueOf("ChannelInfo");
    static final AttributeKey<String> CLOSE_REASON_ATTRIBUTE_KEY =
            AttributeKey.valueOf("CloseReason");
    static final AttributeKey<Integer> CLOSE_CODE_ATTRIBUTE_KEY =
            AttributeKey.valueOf("CloseCode");
    static final AttributeKey<WebSocketServerHandshaker> WS_HANDSHAKER_ATTRIBUTE_KEY =
            AttributeKey.valueOf("WebSocketHandshaker");
    static final String CLOSE_REASON_AUTH_TIMEOUT = "AuthTimeout";
    static final String CLOSE_REASON_NOT_SUPPORTED_METHOD = "NotSupportedMethod";
    static final String CLOSE_REASON_MISSING_PATH = "MissingPath";
    static final String CLOSE_REASON_MISSING_CHANNEL_TYPE = "MissingChannelType";
    static final String CLOSE_REASON_INVALID_PATH = "InvalidPath";
    static final String CLOSE_REASON_ERROR_OCCURRED = "ErrorOccurred";

    private static final Logger LOGGER = LogManager.getLogger(Broker.class);

    private static final String API_REQUEST_PATH = "api";
    private static final String APP_REQUEST_PATH = "app";
    private static final String CLIENT_REQUEST_PATH = "client";
    private static final String PING_REQUEST_PATH = "ping";
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1 MB

    @Autowired
    private SessionCallback sessionCallback;

    @Value("${broker.connection.auth.timeout:5000}") // 5 seconds by default
    private long connectionAuthTimeout;
    @Value("${broker.connection.concurrent.limit:-1}") // No limit by default
    private long connectionConcurrentLimit;
    @Value("${broker.connection.backlog:1024}") // 1024 by default
    private int connectionBacklog;
    @Value("${broker.port:7777}")
    private int portNo;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private MessageHandler messageHandler;


    @Autowired
    @Qualifier("appAuthenticationService")
    private AuthenticationService appAuthenticationService;

    @Autowired
    @Qualifier("clientAuthenticationService")
    private AuthenticationService clientAuthenticationService;

    @Autowired
    @Qualifier("apiAuthenticationServiceImpl")
    private ApiAuthenticationService apiAuthenticationService;

    @Autowired
    private AuthenticationListener authenticationListener;


    private SessionChannelHandler sessionChannelHandler;
    private ServerBootstrap bootstrap;

    public Broker() {
    }

    @PostConstruct
    public void init() throws Exception {

        sessionChannelHandler = new SessionChannelHandler();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap =
                new ServerBootstrap().
                        group(bossGroup, workerGroup).
                        channel(NioServerSocketChannel.class).
                        childHandler(new PeerChannelInitializer(sessionChannelHandler)).
                        option(ChannelOption.SO_BACKLOG, connectionBacklog).
                        childOption(ChannelOption.TCP_NODELAY, true).
                        childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(portNo).sync();
    }

    private static final class PeerChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final ChannelHandler handler;

        private PeerChannelInitializer(ChannelHandler handler) {
            this.handler = handler;
        }


        @Override
        public void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(new HttpServerCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(MAX_FRAME_LENGTH));
            ch.pipeline().addLast(new WebSocketServerCompressionHandler());
            ch.pipeline().addLast(handler);
        }

    }

    @ChannelHandler.Sharable
    private final class SessionChannelHandler extends ChannelDuplexHandler {

        private static final String WEBSOCKET_PATH = "/websocket";

        private static final String TAG_HEADER_NAME_PREFIX = "x-sidekick-tag-";

        private final AtomicLong activeConnectionCounter = new AtomicLong();
        private final ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

        private void sendCloseMessage(ChannelInfo channelInfo,
                                      WebSocketCloseStatus webSocketCloseStatus,
                                      String closeMessage) {
            CloseWebSocketFrame message = new CloseWebSocketFrame(webSocketCloseStatus, closeMessage);
            logWriteMessage(channelInfo, message);
            channelInfo.channel.writeAndFlush(message);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            InetSocketAddress address = (InetSocketAddress) channel.localAddress();

            long activeConnectionCount = activeConnectionCounter.incrementAndGet();
            if (connectionConcurrentLimit >= 0 && activeConnectionCount > connectionConcurrentLimit) {
                LOGGER.error(
                        "Exceeded concurrent connection limit {}. So closing connection (@ {}: {})",
                        connectionConcurrentLimit, address.getPort(), channel.remoteAddress());
                channel.close();
                return;
            }

            String connectionId = UUID.randomUUID().toString();
            ChannelInfo channelInfo = new ChannelInfo(channel, connectionId, System.currentTimeMillis());
            channelInfo.ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
            channelInfo.latestActiveTime = System.currentTimeMillis();
            channel.attr(CHANNEL_INFO_ATTRIBUTE_KEY).set(channelInfo);

            LOGGER.info(
                    "Channel (@ {}: {}) active",
                    address.getPort(), channel.remoteAddress());

            ChannelAuthTimeoutHandler channelAuthTimeoutHandler = new ChannelAuthTimeoutHandler(channelInfo);
            ScheduledFuture future =
                    scheduledExecutorService.schedule(
                            channelAuthTimeoutHandler,
                            connectionAuthTimeout,
                            TimeUnit.MILLISECONDS);
            channelInfo.channelAuthTimeoutHandler = channelAuthTimeoutHandler;
            channelInfo.authFuture = future;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            activeConnectionCounter.decrementAndGet();

            Channel channel = ctx.channel();
            InetSocketAddress address = (InetSocketAddress) channel.localAddress();

            LOGGER.info(
                    "Channel (@ {}: {}) deactive",
                    address.getPort(), channel.remoteAddress());

            Attribute<ChannelInfo> channelInfoAttr = channel.attr(CHANNEL_INFO_ATTRIBUTE_KEY);
            ChannelInfo channelInfo = channelInfoAttr.getAndRemove();
            if (channelInfo != null) {
                sessionService.removeSession(channelInfo, sessionCallback);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                Channel channel = ctx.channel();
                ChannelInfo channelInfo = channel.attr(CHANNEL_INFO_ATTRIBUTE_KEY).get();
                channelInfo.latestActiveTime = System.currentTimeMillis();

                logReadMessage(channelInfo, msg);

                if (msg instanceof FullHttpRequest) {
                    handleHttpRequest(ctx, channelInfo, (FullHttpRequest) msg);
                } else if (msg instanceof WebSocketFrame) {
                    handleWebSocketFrame(ctx, channelInfo, (WebSocketFrame) msg);
                } else {
                    LOGGER.error("Unsupported message type: {}", msg.getClass());
                    sendCloseMessage(
                            channelInfo,
                            WebSocketCloseStatus.INVALID_MESSAGE_TYPE,
                            "Unsupported message type: " + msg.getClass());
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        private void logReadMessage(ChannelInfo channelInfo, Object msg) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Channel ({}) read",
                        channelInfo.channel.remoteAddress());
                if (msg instanceof TextWebSocketFrame) {
                    LOGGER.debug("Message: {}", ((TextWebSocketFrame) msg).text());
                }
            }
        }

        private void logWriteMessage(ChannelInfo channelInfo, Object msg) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Channel ({}) write",
                        channelInfo.channel.remoteAddress());
                if (msg instanceof TextWebSocketFrame) {
                    LOGGER.debug("Message: {}", ((TextWebSocketFrame) msg).text());
                }
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                                      FullHttpRequest req, FullHttpResponse res) {
            logWriteMessage(channelInfo, res);
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!HttpUtil.isKeepAlive(req) || res.status().code() != HttpStatus.OK.value()) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private String getWebSocketLocation(FullHttpRequest req) {
            String location = req.headers().get(HOST) + WEBSOCKET_PATH;
            return "ws://" + location;
        }

        private WebSocketServerHandshaker getWebSocketHandshaker(ChannelInfo channelInfo, FullHttpRequest req) {
            String webSocketLocation = getWebSocketLocation(req);
            WebSocketServerHandshakerFactory wsFactory =
                    new WebSocketServerHandshakerFactory(webSocketLocation, null, true,
                            MAX_FRAME_LENGTH);
            WebSocketServerHandshaker wsHandshaker = wsFactory.newHandshaker(req);
            if (wsHandshaker != null) {
                channelInfo.channel.attr(WS_HANDSHAKER_ATTRIBUTE_KEY).set(wsHandshaker);
            }
            return wsHandshaker;
        }

        private String getForwardedClientIpAddress(FullHttpRequest httpRequest) {
            List<String> forwardedHeaderValues = httpRequest.headers().getAllAsString("X-Forwarded-For");
            if (forwardedHeaderValues != null && forwardedHeaderValues.size() >= 1) {
                String headerValue = forwardedHeaderValues.get(0);
                String[] forwardedHeaderParts = headerValue.split(",");
                return forwardedHeaderParts[0];
            }
            return null;
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req) {
            String forwardedClientIpAddress = getForwardedClientIpAddress(req);
            if (forwardedClientIpAddress != null) {
                channelInfo.ip = forwardedClientIpAddress;
            }

            WebSocketServerHandshaker wsHandshaker = getWebSocketHandshaker(channelInfo, req);
            if (wsHandshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                // Allow only GET methods.
                if (req.method() != GET) {
                    sendHttpResponse(
                            ctx, channelInfo, req,
                            new DefaultFullHttpResponse(
                                    HTTP_1_1,
                                    FORBIDDEN,
                                    Unpooled.copiedBuffer(CLOSE_REASON_NOT_SUPPORTED_METHOD, CharsetUtil.UTF_8)));
                    authenticationListener.onAuthFail(channelInfo, CLOSE_REASON_NOT_SUPPORTED_METHOD);
                    return;
                }

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
                String fullRequestPath = queryStringDecoder.path();
                Optional<String> requestPath =
                        Arrays.stream(fullRequestPath.split("/")).
                                filter(s -> s.length() > 0).
                                findFirst();

                if (!requestPath.isPresent()) {
                    sendHttpResponse(
                            ctx, channelInfo, req,
                            new DefaultFullHttpResponse(
                                    HTTP_1_1,
                                    FORBIDDEN,
                                    Unpooled.copiedBuffer(CLOSE_REASON_MISSING_PATH, CharsetUtil.UTF_8)));
                    authenticationListener.onAuthFail(channelInfo, CLOSE_REASON_MISSING_PATH);
                    return;
                }

                if (PING_REQUEST_PATH.equals(requestPath.get())) {
                    LOGGER.info("Received ping request");
                    sendHttpResponse(
                            ctx, channelInfo, req,
                            new DefaultFullHttpResponse(HTTP_1_1, OK));
                    channelInfo.cancelTimeoutHandler();
                    channelInfo.channel.close();
                    return;
                } else if (APP_REQUEST_PATH.equals(requestPath.get())) {
                    channelInfo.channelType = ChannelType.APP;
                } else if (CLIENT_REQUEST_PATH.equals(requestPath.get())) {
                    channelInfo.channelType = ChannelType.CLIENT;
                } else if (API_REQUEST_PATH.equals(requestPath.get())) {
                    channelInfo.channelType = ChannelType.API;
                } else {
                    sendHttpResponse(
                            ctx, channelInfo, req,
                            new DefaultFullHttpResponse(
                                    HTTP_1_1,
                                    FORBIDDEN,
                                    Unpooled.copiedBuffer(CLOSE_REASON_NOT_SUPPORTED_METHOD, CharsetUtil.UTF_8)));
                    authenticationListener.onAuthFail(channelInfo, CLOSE_REASON_INVALID_PATH);
                    return;
                }

                HttpHeaders headers = req.headers();
                Map<String, String> tags = null;
                for (Map.Entry<String, String> e : headers.entries()) {
                    String headerName = e.getKey();
                    if (headerName.startsWith(TAG_HEADER_NAME_PREFIX)) {
                        String tagName = headerName.substring(TAG_HEADER_NAME_PREFIX.length());
                        String tagValue = e.getValue();
                        if (tags == null) {
                            tags = new HashMap<>();
                        }
                        tags.put(tagName, tagValue);
                    }
                }
                channelInfo.tags = tags;

                Boolean authenticated = null;
                channelInfo.handshakeStarted.set(true);

                switch (channelInfo.channelType) {
                    case APP:
                        authenticated = appAuthenticationService.authenticate(ctx, channelInfo, req, headers);
                        break;
                    case CLIENT:
                        if (clientAuthenticationService != null) {
                            authenticated = clientAuthenticationService.authenticate(ctx, channelInfo, req, headers);
                        } else {
                            authenticated = false;
                        }
                        break;
                    case API:
                        authenticated = apiAuthenticationService.authenticate(ctx, channelInfo, req, headers);
                        break;
                    default:
                        authenticated = false;
                }

                if (authenticated) {
                    authenticationListener.onAuthSuccess(ctx, channelInfo, wsHandshaker, req);
                } else {
                    authenticationListener.onAuthFail(channelInfo, CLOSE_REASON_MISSING_CHANNEL_TYPE);
                }
            }
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, ChannelInfo channelInfo,
                                          WebSocketFrame webSocketFrame) {
            if (webSocketFrame instanceof CloseWebSocketFrame) {
                WebSocketServerHandshaker wsHandshaker = channelInfo.channel.attr(WS_HANDSHAKER_ATTRIBUTE_KEY).get();
                if (wsHandshaker != null) {
                    setCloseReasonAttributes((CloseWebSocketFrame) webSocketFrame, channelInfo);
                    wsHandshaker.close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
                }
            } else if (webSocketFrame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(webSocketFrame.content().retain()));
            } else if (webSocketFrame instanceof WebSocketFrame) {
                messageHandler.handleWebSocketMessage(channelInfo, webSocketFrame);
            }
        }

        private void setCloseReasonAttributes(CloseWebSocketFrame closeWebSocketFrame, ChannelInfo channelInfo) {
            int closeCode = closeWebSocketFrame.statusCode();
            String reasonText = closeWebSocketFrame.reasonText();
            channelInfo.channel.attr(CLOSE_CODE_ATTRIBUTE_KEY).set(closeCode);
            channelInfo.channel.attr(CLOSE_REASON_ATTRIBUTE_KEY).set(reasonText);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel channel = ctx.channel();
            channel.attr(CLOSE_REASON_ATTRIBUTE_KEY).set(CLOSE_REASON_ERROR_OCCURRED);
            InetSocketAddress address = (InetSocketAddress) channel.localAddress();
            LOGGER.error("Error occurred at channel from " + channel.remoteAddress() +
                    " on port " + address.getPort(), cause);
            ctx.close();
        }

    }
}
