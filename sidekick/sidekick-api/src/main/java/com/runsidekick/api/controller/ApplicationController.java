package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.service.BrokerService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@RestController
@RequestMapping("/api/v1/applications")
@Api(value = "/applications", tags = "applications")
public class ApplicationController extends ControllerBase {

    public ApplicationController(ApiAuthService apiAuthService, BrokerService brokerService) {
        super(apiAuthService, brokerService);
    }

    @PostMapping
    public List<Application> listApplications(@RequestBody ListApplicationsRequest listApplicationsRequest) {
        return brokerService.listApplications(getWorkspaceId(), getClient(), listApplicationsRequest);
    }

}
