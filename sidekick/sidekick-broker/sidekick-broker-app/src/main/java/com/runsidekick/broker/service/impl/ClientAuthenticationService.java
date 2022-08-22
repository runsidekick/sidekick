package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.service.AuthenticationService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.runsidekick.broker.util.Constants.ACCOUNT_ID;
import static com.runsidekick.broker.util.Constants.SESSION_GROUP_ID;
import static com.runsidekick.broker.util.Constants.USER_ID;
import static com.runsidekick.broker.util.Constants.WORKSPACE_ID;

@Service
public class ClientAuthenticationService extends AuthenticationService {

    private static final String TOKEN_HEADER_NAME = "x-sidekick-token";
    private static final String AUTH_FAILED = "Auth failed";

    static final String CLOSE_REASON_MISSING_CREDENTIALS = "MissingCredentials";
    static final String CLOSE_REASON_INVALID_CREDENTIALS = "InvalidCredentials";
    static final String CLOSE_MESSAGE_MISSING_CREDENTIALS = "E-Mail & password or token are mandatory in auth request";

    @Value("${broker.token:}")
    private String brokerToken;

    @Override
    public boolean authenticate(ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req,
                                HttpHeaders headers) {
        String token = headers.get(TOKEN_HEADER_NAME);
        if (StringUtils.isEmpty(token)) {
            //Broker Connection  "ws://host:port/client/[TOKEN]";
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            String requestPath = queryStringDecoder.path();
            List<String> requestPaths =
                    Arrays.stream(requestPath.split("/")).
                            filter(s -> s.length() > 0).
                            collect(Collectors.toList());
            token = requestPaths.get(1);
        }

        if (!StringUtils.isEmpty(token)) {
            return tokenAuth(ctx, channelInfo, req, token);
        }

        return authFailed(
                ctx, channelInfo, req, CLOSE_MESSAGE_MISSING_CREDENTIALS, CLOSE_REASON_MISSING_CREDENTIALS);
    }

    private boolean tokenAuth(
            ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req, String token) {

        if (!token.equals(brokerToken)) {
            return authFailed(ctx, channelInfo, req, AUTH_FAILED, CLOSE_REASON_INVALID_CREDENTIALS);
        }

        channelInfo.setUserId(USER_ID);
        channelInfo.setAccountId(ACCOUNT_ID);
        channelInfo.setWorkspaceId(WORKSPACE_ID);
        channelInfo.setSessionId(SessionService.getClientSessionId(SESSION_GROUP_ID, channelInfo.getConnectionId()));
        channelInfo.setSessionGroupId(SESSION_GROUP_ID);
        channelInfo.setChannelMetadata(new ClientMetadata(USER_ID, SESSION_GROUP_ID));
        return true;
    }

}
