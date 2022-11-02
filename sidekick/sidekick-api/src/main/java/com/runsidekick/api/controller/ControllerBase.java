package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.service.BrokerService;
import com.runsidekick.broker.util.Constants;
import lombok.RequiredArgsConstructor;

/**
 * @author yasin.kalafat
 */
@RequiredArgsConstructor
abstract class ControllerBase {

    protected final ApiAuthService apiAuthService;
    protected final BrokerService brokerService;

    protected String getClient() {
        return apiAuthService.getCurrentUser().getApiKey();
    }

    protected String getUserId() {
        return Constants.USER_ID;
    }

    protected String getWorkspaceId() {
        return Constants.WORKSPACE_ID;
    }
}
