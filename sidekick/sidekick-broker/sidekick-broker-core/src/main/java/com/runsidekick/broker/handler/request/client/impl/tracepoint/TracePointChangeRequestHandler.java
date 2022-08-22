package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ozge.lule
 */
public abstract class TracePointChangeRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    protected TracePointChangeRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected Set<String> filterApplications(String workspaceId, String tracePointId, List<String> applications) {
        Set<String> applicationInstanceIds = new HashSet<>();

        if (applications != null) {
            applicationInstanceIds.addAll(applications);
        }

        if (tracePointId != null) {
            Collection<Application> allApps = applicationService.listApplications(workspaceId);
            for (Application app: allApps) {
                if (app.getTracePoints() != null) {
                    for (TracePoint tracePoint : app.getTracePoints()) {
                        String appTracePointId = tracePoint.getId();
                        if (appTracePointId != null && appTracePointId.equals(tracePointId)) {
                            applicationInstanceIds.add(app.getInstanceId());
                        }
                    }
                }
            }
        }


        return applicationInstanceIds;
    }

}
