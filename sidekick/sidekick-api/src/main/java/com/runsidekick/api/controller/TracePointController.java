package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.DisableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.EnableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;
import com.runsidekick.broker.service.BrokerService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
@RestController
@RequestMapping("/api/v1/tracepoint")
@Api(value = "/tracepoint", tags = "tracepoint")
public class TracePointController extends ControllerBase {

    public TracePointController(ApiAuthService apiAuthService, BrokerService brokerService) {
        super(apiAuthService, brokerService);
    }

    @GetMapping
    public List<TracePoint> listTracePoints() {
        return brokerService.listTracePoints(getWorkspaceId(), getUserId());
    }

    @PostMapping
    public CompletableFuture<CompositeResponse<PutTracePointResponse>> putTracePoint(
            @Valid @RequestBody PutTracePointRequest request) throws Exception {
        return brokerService.putTracePoint(request, getClient(), getWorkspaceId());
    }

    @PutMapping
    public CompletableFuture<CompositeResponse<UpdateTracePointResponse>> updateTracePoint(
            @Valid @RequestBody UpdateTracePointRequest request) throws Exception {
        return brokerService.updateTracePoint(request, getClient(), getWorkspaceId());
    }

    @DeleteMapping
    public CompletableFuture<CompositeResponse<RemoveTracePointResponse>> removeTracePoint(
            @Valid @RequestBody String tracePointId) throws Exception {
        return brokerService.removeTracePoint(tracePointId, getClient(), getWorkspaceId());
    }

    @PutMapping("/enable")
    public CompletableFuture<CompositeResponse<EnableTracePointResponse>> enableTracePoint(
            @Valid @RequestBody String tracePointId) throws Exception {
        return brokerService.enableTracePoint(tracePointId, getClient(), getWorkspaceId());
    }

    @PutMapping("/disable")
    public CompletableFuture<CompositeResponse<DisableTracePointResponse>> disableTracePoint(
            @Valid @RequestBody String tracePointId) throws Exception {
        return brokerService.disableTracePoint(tracePointId, getClient(), getWorkspaceId());
    }

}
