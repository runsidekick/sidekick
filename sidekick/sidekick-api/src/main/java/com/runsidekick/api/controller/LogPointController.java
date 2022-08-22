package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.UpdateLogPointRequest;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import com.runsidekick.broker.model.response.impl.logpoint.DisableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.EnableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.UpdateLogPointResponse;
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
@RequestMapping("/api/v1/logpoint")
@Api(value = "/logpoint", tags = "logpoint")
public class LogPointController extends ControllerBase {

    public LogPointController(ApiAuthService apiAuthService, BrokerService brokerService) {
        super(apiAuthService, brokerService);
    }

    @GetMapping
    public List<LogPoint> listLogPoints() {
        return brokerService.listLogPoints(getWorkspaceId(), getUserId());
    }

    @PostMapping
    public CompletableFuture<CompositeResponse<PutLogPointResponse>> putLogPoint(
            @Valid @RequestBody PutLogPointRequest request) throws Exception {
        return brokerService.putLogPoint(request, getClient(), getWorkspaceId());
    }

    @PutMapping
    public CompletableFuture<CompositeResponse<UpdateLogPointResponse>> updateLogPoint(
            @Valid @RequestBody UpdateLogPointRequest request) throws Exception {
        return brokerService.updateLogPoint(request, getClient(), getWorkspaceId());
    }

    @DeleteMapping
    public CompletableFuture<CompositeResponse<RemoveLogPointResponse>> removeLogPoint(
            @Valid @RequestBody String logPointId) throws Exception {
        return brokerService.removeLogPoint(logPointId, getClient(), getWorkspaceId());
    }

    @PutMapping("/enable")
    public CompletableFuture<CompositeResponse<EnableLogPointResponse>> enableLogPoint(
            @Valid @RequestBody String logPointId) throws Exception {
        return brokerService.enableLogPoint(logPointId, getClient(), getWorkspaceId());
    }

    @PutMapping("/disable")
    public CompletableFuture<CompositeResponse<DisableLogPointResponse>> disableLogPoint(
            @Valid @RequestBody String logPointId) throws Exception {
        return brokerService.disableLogPoint(logPointId, getClient(), getWorkspaceId());
    }
}
