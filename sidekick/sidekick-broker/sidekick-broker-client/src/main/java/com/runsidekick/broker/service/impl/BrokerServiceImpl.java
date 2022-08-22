package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.client.ClientCredentials;
import com.runsidekick.broker.client.SidekickBrokerClient;
import com.runsidekick.broker.exception.WSClientNotConnectedException;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.BaseProbe;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.model.request.impl.logpoint.DisableLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.EnableLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.UpdateLogPointRequest;
import com.runsidekick.broker.model.request.impl.refereceevent.PutReferenceEventRequest;
import com.runsidekick.broker.model.request.impl.refereceevent.RemoveReferenceEventRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.DisableTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.EnableTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import com.runsidekick.broker.model.response.impl.logpoint.DisableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.EnableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.UpdateLogPointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.DisableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.EnableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;
import com.runsidekick.broker.service.ApplicationService;
import com.runsidekick.broker.service.BrokerService;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.ReferenceEventService;
import com.runsidekick.broker.service.TracePointService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
@Service
@RequiredArgsConstructor
public class BrokerServiceImpl implements BrokerService {

    private final Logger logger = LogManager.getLogger(getClass());

    @Value("${broker.client.authToken:}")
    private String brokerClientAuthToken;

    @Value("${broker.url:localhost}")
    private String brokerUrl;

    @Value("${broker.port:7777}")
    private int brokerPort;

    //CHECKSTYLE:OFF
    @Value("${broker.client.max_connection_retry_count:100}")
    private int maxConnectionRetryCount;

    @Value("${broker.client.connection_retry_interval:30000}")
    private int connectionRetryInterval;
    //CHECKSTYLE:ON

    private SidekickBrokerClient client;

    private int retryCount = 0;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogPointService logPointService;

    @Autowired
    private ReferenceEventService referenceEventService;

    @Autowired
    private TracePointService tracePointService;

    @PostConstruct
    public void initBrokerClient() {
        CompletableFuture connectFuture = new CompletableFuture();
        client = new SidekickBrokerClient(brokerUrl, brokerPort, new ClientCredentials(null, brokerClientAuthToken),
                new CompletableConnectionCallback(connectFuture, null));
    }

    private synchronized SidekickBrokerClient ensureConnected() throws Exception {
        if (client == null || !client.isConnected() || client.isClosed()) {
            throw new WSClientNotConnectedException("Client is not connected to broker");
        }
        return client;
    }

    private void reconnect() {
        if (retryCount++ < maxConnectionRetryCount) {
            logger.debug("connection failed retry connection " + retryCount);
            try {
                Thread.sleep(connectionRetryInterval);
            } catch (InterruptedException e) {
            }
            initBrokerClient();
        }
    }

    private final class CompletableConnectionCallback implements SidekickBrokerClient.ConnectionCallback {

        private final CompletableFuture connectFuture;
        private final CompletableFuture closeFuture;

        private CompletableConnectionCallback(CompletableFuture connectFuture, CompletableFuture closeFuture) {
            this.connectFuture = connectFuture;
            this.closeFuture = closeFuture;
        }

        @Override
        public void onConnectSuccess(SidekickBrokerClient client) {
            if (connectFuture != null) {
                connectFuture.complete(null);
                retryCount = 0;
            }
        }

        @Override
        public void onConnectFailure(SidekickBrokerClient client, Throwable t) {
            if (connectFuture != null) {
                connectFuture.completeExceptionally(t);
            }
            reconnect();
        }

        @Override
        public void onClose(SidekickBrokerClient client, Throwable t) {
            if (client != null) {
                client.destroy();
            }
            setClient(null);
            if (closeFuture != null) {
                if (t != null) {
                    closeFuture.completeExceptionally(t);
                } else {
                    closeFuture.complete(null);
                }
            }
        }
    }

    public void setClient(SidekickBrokerClient client) {
        this.client = client;
    }

    private void prepareRequest(BaseApplicationAwareRequest request, String client, String workspaceId) {
        String requestId = UUID.randomUUID().toString();
        request.setId(requestId);
        request.setClient(client);
        request.setWorkspaceId(workspaceId);
        request.setPersist(true);
    }

    @Override
    public CompletableFuture<CompositeResponse<PutTracePointResponse>> putTracePoint(
            PutTracePointRequest putTracePointRequest, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        prepareRequest(putTracePointRequest, email, workspaceId);

        return client.requestAll(
                putTracePointRequest,
                PutTracePointResponse.class,
                putTracePointRequest.getApplicationFilters());
    }

    @Override
    public CompletableFuture<CompositeResponse<UpdateTracePointResponse>> updateTracePoint(
            UpdateTracePointRequest updateTracePointRequest, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        prepareRequest(updateTracePointRequest, email, workspaceId);

        return client.requestAll(updateTracePointRequest, UpdateTracePointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<RemoveTracePointResponse>> removeTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        RemoveTracePointRequest removeTracePointRequest = new RemoveTracePointRequest();
        removeTracePointRequest.setTracePointId(tracePointId);

        prepareRequest(removeTracePointRequest, email, workspaceId);

        return client.requestAll(removeTracePointRequest, RemoveTracePointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<EnableTracePointResponse>> enableTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        EnableTracePointRequest enableTracePointRequest = new EnableTracePointRequest();
        enableTracePointRequest.setTracePointId(tracePointId);

        prepareRequest(enableTracePointRequest, email, workspaceId);

        return client.requestAll(enableTracePointRequest, EnableTracePointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<DisableTracePointResponse>> disableTracePoint(
            String tracePointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        DisableTracePointRequest disableTracePointRequest = new DisableTracePointRequest();
        disableTracePointRequest.setTracePointId(tracePointId);

        prepareRequest(disableTracePointRequest, email, workspaceId);

        return client.requestAll(disableTracePointRequest, DisableTracePointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<PutLogPointResponse>> putLogPoint(
            PutLogPointRequest putLogPointRequest, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        prepareRequest(putLogPointRequest, email, workspaceId);

        return client.requestAll(
                putLogPointRequest,
                PutLogPointResponse.class,
                putLogPointRequest.getApplicationFilters());
    }

    @Override
    public CompletableFuture<CompositeResponse<UpdateLogPointResponse>> updateLogPoint(
            UpdateLogPointRequest updateLogPointRequest, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        prepareRequest(updateLogPointRequest, email, workspaceId);

        return client.requestAll(updateLogPointRequest, UpdateLogPointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<RemoveLogPointResponse>> removeLogPoint(
            String logPointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        RemoveLogPointRequest removeLogPointRequest = new RemoveLogPointRequest();
        removeLogPointRequest.setLogPointId(logPointId);

        prepareRequest(removeLogPointRequest, email, workspaceId);

        return client.requestAll(removeLogPointRequest, RemoveLogPointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<EnableLogPointResponse>> enableLogPoint(
            String logPointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        EnableLogPointRequest enableLogPointRequest = new EnableLogPointRequest();
        enableLogPointRequest.setLogPointId(logPointId);

        prepareRequest(enableLogPointRequest, email, workspaceId);

        return client.requestAll(enableLogPointRequest, EnableLogPointResponse.class);
    }

    @Override
    public CompletableFuture<CompositeResponse<DisableLogPointResponse>> disableLogPoint(
            String logPointId, String email, String workspaceId) throws Exception {
        SidekickBrokerClient client = ensureConnected();

        DisableLogPointRequest disableLogPointRequest = new DisableLogPointRequest();
        disableLogPointRequest.setLogPointId(logPointId);

        prepareRequest(disableLogPointRequest, email, workspaceId);

        return client.requestAll(disableLogPointRequest, DisableLogPointResponse.class);
    }

    @Override
    public List<Application> listApplications(String workspaceId, String client,
                                              ListApplicationsRequest listApplicationsRequest) {
        return applicationService.listApplications(workspaceId, client, listApplicationsRequest);
    }

    @Override
    public List<LogPoint> listLogPoints(String workspaceId, String userId) {
        return logPointService.listLogPoints(workspaceId, userId);
    }

    @Override
    public List<LogPoint> listPredefinedLogPoints(String workspaceId, String userId) {
        return logPointService.listPredefinedLogPoints(workspaceId, userId);
    }

    @Override
    public List<TracePoint> listTracePoints(String workspaceId, String userId) {
        return tracePointService.listTracePoints(workspaceId, userId);
    }

    @Override
    public List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId) {
        return tracePointService.listPredefinedTracePoints(workspaceId, userId);
    }

    @Override
    public ReferenceEvent getReferenceEvent(String id, ProbeType probeType) {
        return referenceEventService.getReferenceEvent(id, probeType);
    }

    @Override
    public void saveReferenceEvent(PutReferenceEventRequest request) throws Exception {
        BaseProbe probe = null;
        // Only Predefined Probes
        if (request.getProbeType().equals(ProbeType.TRACEPOINT)) {
            probe = tracePointService.getTracePointById(request.getProbeId());
        } else if (request.getProbeType().equals(ProbeType.LOGPOINT)) {
            probe = logPointService.getLogPointById(request.getProbeId());
        }

        if (probe != null && probe.isPredefined()) {
            referenceEventService.putReferenceEvent(ReferenceEvent.builder()
                    .probeId(request.getProbeId())
                    .probeType(request.getProbeType())
                    .event(request.getEvent())
                    .build());
        } else {
            throw new Exception("No Predefined Probe Found");
        }
    }

    @Override
    public void removeReferenceEvent(RemoveReferenceEventRequest request) {
        referenceEventService.removeReferenceEvent(request.getProbeId(), request.getProbeType());
    }
}
