package com.runsidekick.service.impl;

import com.runsidekick.model.Webhook;
import com.runsidekick.repository.WebhookRepository;
import com.runsidekick.service.WebhookService;
import io.thundra.swark.common.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * @author yasin.kalafat
 */
@Service
public class WebhookServiceImpl implements WebhookService {

    @Autowired
    private WebhookRepository webhookRepository;

    @Override
    @Cacheable(cacheNames = "Webhook", key = "#id")
    public Webhook get(String id) {
        return webhookRepository.findById(id);
    }

    @Override
    public List<Webhook> listByWorkspaceId(String workspaceId) {
        return webhookRepository.listByWorkspaceId(workspaceId);
    }

    @Override
    @CacheEvict(cacheNames = "Webhook", key = "#webhook.id")
    public Webhook add(Webhook webhook) {
        webhook.setId(UUID.randomUUID().toString());
        webhook.setDisabled(false);
        webhookRepository.save(webhook);
        return webhook;
    }

    @Override
    @CacheEvict(cacheNames = "Webhook", key = "#id")
    public Webhook update(String id, Webhook webhook) {
        Webhook existingWebhook = webhookRepository.findById(id);
        if (existingWebhook == null) {
            throw new EntityNotFoundException("No webhook found with id " + id);
        }
        existingWebhook.setType(webhook.getType());
        if (StringUtils.hasText(webhook.getConfig())) {
            existingWebhook.setConfig(webhook.getConfig());
        }
        webhookRepository.save(existingWebhook);
        return existingWebhook;
    }

    @Override
    @CacheEvict(cacheNames = "Webhook", key = "#id")
    public Webhook enable(String id) {
        Webhook existingWebhook = webhookRepository.findById(id);
        if (existingWebhook == null) {
            throw new EntityNotFoundException("No webhook found with id " + id);
        }
        existingWebhook.setDisabled(false);
        webhookRepository.save(existingWebhook);
        return existingWebhook;
    }

    @Override
    @CacheEvict(cacheNames = "Webhook", key = "#id")
    public void delete(String id) {
        webhookRepository.delete(id);
    }
}
