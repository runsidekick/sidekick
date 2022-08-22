package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yasin.kalafat
 */
public abstract class LogPointChangeRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    protected LogPointChangeRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected Set<String> filterApplications(String workspaceId, String logPointId, List<String> applications) {
        Set<String> applicationInstanceIds = new HashSet<>();

        if (applications != null) {
            applicationInstanceIds.addAll(applications);
        }

        if (logPointId != null) {
            Collection<Application> allApps = applicationService.listApplications(workspaceId);
            for (Application app: allApps) {
                if (app.getLogPoints() != null) {
                    for (LogPoint logPoint : app.getLogPoints()) {
                        String appLogPointId = logPoint.getId();
                        if (appLogPointId != null && appLogPointId.equals(logPointId)) {
                            applicationInstanceIds.add(app.getInstanceId());
                        }
                    }
                }
            }
        }


        return applicationInstanceIds;
    }

}
