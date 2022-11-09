package com.runsidekick.broker.handler.request.client.impl.config;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.request.impl.config.BaseConfigRequest;
import com.runsidekick.broker.model.response.Response;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yasin.kalafat
 */
public abstract class BaseApplicationConfigRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    public BaseApplicationConfigRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected Set<String> filterApplications(String workspaceId, BaseConfigRequest request) {
        Set<String> apps = new HashSet<>();

        if (request.getApplications() != null) {
            apps.addAll(request.getApplications());
        }
        apps.addAll(applicationService.filterApplications(workspaceId, request.getApplicationFilters()));
        return apps;
    }
}
