package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
public abstract class BatchTracePointChangeRequestHandler<Req extends Request, Res extends Response>
        extends BaseClientRequestHandler<Req, Res> {

    public BatchTracePointChangeRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        super(requestName, requestClass, responseClass);
    }

    protected List<String> filterApplications(String workspaceId, List<String> tracePointIds) {
        List<String> applicationInstanceIds = new ArrayList<>();

        Collection<Application> allApps = applicationService.listApplications(workspaceId);
        for (Application app : allApps) {
            if (app.getTracePoints() != null) {
                for (TracePoint tracePoint : app.getTracePoints()) {
                    String appTracePointId = tracePoint.getId();
                    if (appTracePointId != null && tracePointIds.contains(appTracePointId)) {
                        applicationInstanceIds.add(app.getInstanceId());
                    }
                }
            }
        }
        return applicationInstanceIds.stream().distinct().collect(Collectors.toList());
    }
}
