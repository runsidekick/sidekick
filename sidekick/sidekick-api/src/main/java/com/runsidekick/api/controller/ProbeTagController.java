package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.model.request.impl.probetag.DisableProbeTagRequest;
import com.runsidekick.broker.model.request.impl.probetag.EnableProbeTagRequest;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import com.runsidekick.broker.model.response.impl.probetag.DisableProbeTagResponse;
import com.runsidekick.broker.model.response.impl.probetag.EnableProbeTagResponse;
import com.runsidekick.broker.service.BrokerService;
import com.runsidekick.model.ProbeTag;
import com.runsidekick.service.ProbeTagService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/v1/probetags")
@Api(value = "/probetags", tags = "probetags")
public class ProbeTagController extends ControllerBase {

    private final ProbeTagService probeTagService;

    public ProbeTagController(ApiAuthService apiAuthService, BrokerService brokerService,
                              ProbeTagService probeTagService) {
        super(apiAuthService, brokerService);
        this.probeTagService = probeTagService;
    }

    @GetMapping
    public List<ProbeTag> listTags() {
        return probeTagService.listByWorkspaceId(getWorkspaceId());
    }

    @PostMapping
    public ProbeTag addTag(@Valid @RequestBody ProbeTag probeTag) {
        probeTag.setWorkspaceId(getWorkspaceId());
        return probeTagService.add(probeTag);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteTag(@PathVariable String id) {
        probeTagService.delete(id);
    }

    @PutMapping("/enable")
    public CompletableFuture<CompositeResponse<EnableProbeTagResponse>> enableProbeTag(
            @Valid @RequestBody EnableProbeTagRequest request) throws Exception {
        return brokerService.enableProbeTag(request, getClient(), getWorkspaceId());
    }

    @PutMapping("/disable")
    public CompletableFuture<CompositeResponse<DisableProbeTagResponse>> disableProbeTag(
            @Valid @RequestBody DisableProbeTagRequest request) throws Exception {
        return brokerService.disableProbeTag(request, getClient(), getWorkspaceId());
    }

}
