package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
public abstract class BatchLogPointChangeRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    public BatchLogPointChangeRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected List<String> filterApplications(String workspaceId, List<String> logPointIds) {
        List<String> applicationInstanceIds = new ArrayList<>();

        Collection<Application> allApps = applicationService.listApplications(workspaceId);
        for (Application app : allApps) {
            if (app.getLogPoints() != null) {
                for (LogPoint logPoint : app.getLogPoints()) {
                    String appLogPointId = logPoint.getId();
                    if (appLogPointId != null && logPointIds.contains(appLogPointId)) {
                        applicationInstanceIds.add(app.getInstanceId());
                    }
                }
            }
        }
        return applicationInstanceIds.stream().distinct().collect(Collectors.toList());
    }
}
