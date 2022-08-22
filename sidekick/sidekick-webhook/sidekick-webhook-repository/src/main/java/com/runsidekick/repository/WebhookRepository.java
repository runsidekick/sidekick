package com.runsidekick.repository;

import com.runsidekick.model.Webhook;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface WebhookRepository {

    List<Webhook> listByWorkspaceId(String workspaceId);

    void save(Webhook webhook);

    void delete(String id);

    Webhook findById(String id);
}
