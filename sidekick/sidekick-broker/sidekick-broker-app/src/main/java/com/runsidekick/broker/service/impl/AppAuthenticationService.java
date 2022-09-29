package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.service.ApplicationService;
import com.runsidekick.broker.service.AuthenticationService;
import com.runsidekick.service.ServerStatisticsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.runsidekick.broker.util.Constants.WORKSPACE_ID;

@Service
public class AppAuthenticationService extends AuthenticationService {

    private static final String API_KEY_HEADER_NAME = "x-sidekick-api-key";
    private static final String APP_INSTANCE_ID_HEADER_NAME = "x-sidekick-app-instance-id";
    private static final String APP_NAME_HEADER_NAME = "x-sidekick-app-name";
    private static final String APP_STAGE_HEADER_NAME = "x-sidekick-app-stage";
    private static final String APP_VERSION_HEADER_NAME = "x-sidekick-app-version";
    private static final String APP_HOSTNAME_HEADER_NAME = "x-sidekick-app-hostname";
    private static final String APP_RUNTIME_HEADER_NAME = "x-sidekick-app-runtime";
    private static final String APP_TAGS_HEADER_PREFIX = "x-sidekick-app-tag-";

    static final String CLOSE_REASON_MISSING_CREDENTIALS = "MissingCredentials";
    static final String CLOSE_REASON_INVALID_CREDENTIALS = "InvalidCredentials";

    private final ApplicationService applicationService;
    private final ServerStatisticsService serverStatisticsService;

    @Value("${broker.token:}")
    private String brokerToken;

    public AppAuthenticationService(ApplicationService applicationService,
                                    ServerStatisticsService serverStatisticsService) {
        this.applicationService = applicationService;
        this.serverStatisticsService = serverStatisticsService;
    }

    @Override
    public boolean authenticate(ChannelHandlerContext ctx, ChannelInfo channelInfo, FullHttpRequest req,
                                HttpHeaders headers) {

        String apiKey = headers.get(API_KEY_HEADER_NAME);
        String appInstanceId = headers.get(APP_INSTANCE_ID_HEADER_NAME);
        String appName = headers.get(APP_NAME_HEADER_NAME);
        String appStage = headers.get(APP_STAGE_HEADER_NAME);
        String appVersion = headers.get(APP_VERSION_HEADER_NAME);
        String appHostName = headers.get(APP_HOSTNAME_HEADER_NAME);
        String appRuntime = headers.get(APP_RUNTIME_HEADER_NAME);

        List<Application.CustomTag> appCustomTags = new ArrayList<>();
        Map<String, String> customTags = new HashMap<>();
        for (Map.Entry<String, String> header : headers) {
            if (header.getKey().startsWith(APP_TAGS_HEADER_PREFIX)) {
                String tagName = header.getKey().replace(APP_TAGS_HEADER_PREFIX, "");
                String tagValue = header.getValue();
                appCustomTags.add(new Application.CustomTag(tagName, tagValue));
                customTags.put(tagName, tagValue);
            }
        }

        if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(appInstanceId)) {
            String closeMessage = "Api key and application instance id mandatory in auth request";
            return authFailed(ctx, channelInfo, req, closeMessage, CLOSE_REASON_MISSING_CREDENTIALS);
        }

        if (!apiKey.equals(brokerToken)) {
            String closeMessage = "Token mismatch";
            return authFailed(ctx, channelInfo, req, closeMessage, CLOSE_REASON_INVALID_CREDENTIALS);
        }

        channelInfo.setWorkspaceId(WORKSPACE_ID);
        channelInfo.setSessionId(SessionService.getAppSessionId(appInstanceId));
        channelInfo.setChannelMetadata(
                new ApplicationMetadata(
                        appInstanceId, appName, appStage, appVersion,
                        channelInfo.getIp(), appHostName, appRuntime, appCustomTags));

        serverStatisticsService.increaseApplicationInstanceCount(WORKSPACE_ID);

        return true;
    }
}
