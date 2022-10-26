package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yasin.kalafat
 */
public abstract class BaseProbeTagRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    protected BaseProbeTagRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected Set<String> filterApplications(String workspaceId) {
        Set<String> apps = new HashSet<>();
        Collection<Application> applications = applicationService.listApplications(workspaceId);
        applications.forEach(application -> apps.add(application.getInstanceId()));
        return apps;
    }

}
