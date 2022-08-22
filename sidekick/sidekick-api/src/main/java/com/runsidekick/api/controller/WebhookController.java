package com.runsidekick.api.controller;

import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.service.BrokerService;
import com.runsidekick.model.Webhook;
import com.runsidekick.model.dto.WebhookDto;
import com.runsidekick.service.WebhookService;
import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Api(value = "/webhooks", tags = "webhooks")
public class WebhookController extends ControllerBase {

    private final WebhookService webhookService;

    public WebhookController(ApiAuthService apiAuthService, BrokerService brokerService,
                             WebhookService webhookService) {
        super(apiAuthService, brokerService);
        this.webhookService = webhookService;
    }

    @GetMapping
    public ResponseEntity<List<WebhookDto>> listWebhooks() {
        return ResponseEntity.ok(webhookService.listByWorkspaceId(getWorkspaceId())
                .stream().map(WebhookDto::convert).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Webhook> addWebhook(@Valid @RequestBody Webhook webhook) {
        webhook.setWorkspaceId(getWorkspaceId());
        return ResponseEntity.ok(webhookService.add(webhook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookDto> updateWebhook(@PathVariable String id, @Valid @RequestBody Webhook webhook) {
        webhook.setWorkspaceId(getWorkspaceId());
        Webhook updatedWebhook = webhookService.update(id, webhook);
        return ResponseEntity.ok(WebhookDto.convert(updatedWebhook));
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<WebhookDto> enable(@PathVariable String id) {
        Webhook enabledWebhook = webhookService.enable(id);
        return ResponseEntity.ok(WebhookDto.convert(enabledWebhook));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable String id) {
        Webhook webhook = webhookService.get(id);
        if (webhook != null) {
            webhookService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
