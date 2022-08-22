package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.service.ApiAuthenticationService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.runsidekick.broker.util.Constants.ACCOUNT_ID;
import static com.runsidekick.broker.util.Constants.SESSION_GROUP_ID;
import static com.runsidekick.broker.util.Constants.USER_ID;
import static com.runsidekick.broker.util.Constants.WORKSPACE_ID;

@Service
public class ApiAuthenticationServiceImpl extends ApiAuthenticationService {

    private static final String TOKEN_HEADER_NAME = "x-sidekick-token";

    static final String CLOSE_REASON_MISSING_CREDENTIALS = "MissingCredentials";
    static final String CLOSE_REASON_INVALID_CREDENTIALS = "InvalidCredentials";

    @Value("${broker.token:}")
    private String brokerToken;

    protected ApiAuthenticationServiceImpl(SessionService sessionService) {
        super(sessionService);
    }

    @Override
    public ChannelInfo generateClientChannelInfo(ChannelInfo channelInfo, String email, String workspaceId)
            throws Exception {
        ChannelInfo userChannelInfo =
                new ChannelInfo(channelInfo.getChannel(), channelInfo.getConnectionId(), channelInfo.getConnectTime());

        userChannelInfo.setChannelType(channelInfo.getChannelType());
        userChannelInfo.setUserId(USER_ID);
        userChannelInfo.setAccountId(ACCOUNT_ID);
        userChannelInfo.setWorkspaceId(WORKSPACE_ID);
        userChannelInfo.setSessionGroupId(SESSION_GROUP_ID);
        userChannelInfo.setChannelMetadata(new ClientMetadata(USER_ID, SESSION_GROUP_ID));
        userChannelInfo.setSessionId(UUID.randomUUID().toString());
        getSessionService().addSession(userChannelInfo, null);
        return userChannelInfo;
    }

    @Override
    public boolean authenticate(ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req,
                                HttpHeaders headers) {

        String token = headers.get(TOKEN_HEADER_NAME);

        if (StringUtils.isEmpty(token)) {
            String closeMessage = "Token mandatory in auth request";
            return authFailed(ctx, channelInfo, req, closeMessage, CLOSE_REASON_MISSING_CREDENTIALS);
        }

        if (!token.equals(brokerToken)) {
            String closeMessage = "Token mismatch";
            return authFailed(ctx, channelInfo, req, closeMessage, CLOSE_REASON_INVALID_CREDENTIALS);
        }

        return true;
    }

}
