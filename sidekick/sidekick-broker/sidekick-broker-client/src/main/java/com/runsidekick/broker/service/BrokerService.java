package com.runsidekick.broker.service;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.UpdateLogPointRequest;
import com.runsidekick.broker.model.request.impl.refereceevent.PutReferenceEventRequest;
import com.runsidekick.broker.model.request.impl.refereceevent.RemoveReferenceEventRequest;
import com.runsidekick.broker.model.request.impl.probetag.DisableProbeTagRequest;
import com.runsidekick.broker.model.request.impl.probetag.EnableProbeTagRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import com.runsidekick.broker.model.response.impl.logpoint.DisableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.EnableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.UpdateLogPointResponse;
import com.runsidekick.broker.model.response.impl.probetag.DisableProbeTagResponse;
import com.runsidekick.broker.model.response.impl.probetag.EnableProbeTagResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.DisableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.EnableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
public interface BrokerService {

    CompletableFuture<CompositeResponse<PutTracePointResponse>> putTracePoint(
            PutTracePointRequest putTracePointRequest, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<UpdateTracePointResponse>> updateTracePoint(
            UpdateTracePointRequest updateTracePointRequest, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<RemoveTracePointResponse>> removeTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<EnableTracePointResponse>> enableTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<DisableTracePointResponse>> disableTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<PutLogPointResponse>> putLogPoint(
            PutLogPointRequest putLogPointRequest, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<UpdateLogPointResponse>> updateLogPoint(
            UpdateLogPointRequest updateLogPointRequest, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<RemoveLogPointResponse>> removeLogPoint(
            String logPointId, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<EnableLogPointResponse>> enableLogPoint(
            String logPointId, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<DisableLogPointResponse>> disableLogPoint(
            String logPointId, String email, String workspaceId) throws Exception;

    List<Application> listApplications(String workspaceId, String client,
                                       ListApplicationsRequest listApplicationsRequest);

    List<LogPoint> listLogPoints(String workspaceId, String userId);

    List<LogPoint> listPredefinedLogPoints(String workspaceId, String userId);

    List<TracePoint> listTracePoints(String workspaceId, String userId);

    List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId);

    ReferenceEvent getReferenceEvent(String workspaceId, String id, ProbeType probeType,
                                     ApplicationFilter applicationFilter);

    void saveReferenceEvent(String workspaceId, PutReferenceEventRequest request) throws Exception;

    void removeReferenceEvent(String workspaceId, RemoveReferenceEventRequest request);

    CompletableFuture<CompositeResponse<EnableProbeTagResponse>> enableProbeTag(
            EnableProbeTagRequest request, String email, String workspaceId) throws Exception;

    CompletableFuture<CompositeResponse<DisableProbeTagResponse>> disableProbeTag(
            DisableProbeTagRequest request, String email, String workspaceId) throws Exception;
}
