package com.runsidekick.broker.handler.request.app.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.config.GetConfigRequest;
import com.runsidekick.broker.model.response.impl.config.GetConfigResponse;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Component
public class GetConfigRequestHandler extends BaseAppRequestHandler<GetConfigRequest, GetConfigResponse> {
    public static final String REQUEST_NAME = "GetConfigRequest";

    @Autowired
    private ApplicationConfigService applicationConfigService;

    public GetConfigRequestHandler() {
        super(REQUEST_NAME, GetConfigRequest.class, GetConfigResponse.class);
    }

    @Override
    public GetConfigResponse handleRequest(
            ChannelInfo channelInfo, GetConfigRequest request, RequestContext requestContext) {
        ApplicationMetadata channelMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();

        Map<String, String> customTags = new HashMap<>();
        channelMetadata.getCustomTags().forEach(customTag ->
                customTags.put(customTag.getTagName(), customTag.getTagValue()));

        ApplicationConfig applicationConfig = applicationConfigService.getApplicationConfig(
                channelInfo.getWorkspaceId(), ApplicationFilter.builder()
                                .name(channelMetadata.getName())
                                .stage(channelMetadata.getStage())
                        .version(channelMetadata.getVersion())
                        .build());

        GetConfigResponse response = new GetConfigResponse();
        response.setApplicationInstanceId(channelMetadata.getInstanceId());
        response.setConfig(applicationConfig.getConfig());
        response.setRequestId(request.getId());

        return response;
    }
}
