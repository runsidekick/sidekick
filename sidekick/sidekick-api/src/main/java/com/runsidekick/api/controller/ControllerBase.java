package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.service.BrokerService;
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
        return apiAuthService.getCurrentUser().getApiKey();
    }

    protected String getWorkspaceId() {
        return apiAuthService.getCurrentUser().getApiKey();
    }
}
