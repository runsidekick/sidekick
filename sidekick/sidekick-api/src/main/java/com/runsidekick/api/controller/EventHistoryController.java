package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.service.BrokerService;
import com.runsidekick.model.EventHistory;
import com.runsidekick.model.EventHitCount;
import com.runsidekick.model.request.EventHistoryRequest;
import com.runsidekick.service.EventHistoryService;
import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@RestController
@RequestMapping("/api/v1/eventhistory")
@Api(value = "/eventhistory", tags = "eventhistory")
public class EventHistoryController extends ControllerBase {

    private final EventHistoryService eventHistoryService;

    public EventHistoryController(ApiAuthService apiAuthService, BrokerService brokerService,
                                  EventHistoryService eventHistoryService) {
        super(apiAuthService, brokerService);
        this.eventHistoryService = eventHistoryService;
    }

    @PostMapping
    public ResponseEntity<List<EventHistory>> listEventHistory(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestBody EventHistoryRequest request) {
        request.setWorkspaceId(getWorkspaceId());
        return ResponseEntity.ok(eventHistoryService.queryEventHistory(request, page, size));
    }

    @PostMapping("/count")
    public ResponseEntity<List<EventHitCount>> getCountsGroupedByDate(@RequestBody EventHistoryRequest request) {
        request.setWorkspaceId(getWorkspaceId());
        return ResponseEntity.ok(eventHistoryService.getCountsGroupedByDate(request));
    }

}
