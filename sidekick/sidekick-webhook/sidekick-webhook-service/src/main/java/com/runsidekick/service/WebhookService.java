package com.runsidekick.service;

import com.runsidekick.model.Webhook;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface WebhookService {

    Webhook get(String id);

    List<Webhook> listByWorkspaceId(String workspaceId);

    Webhook add(Webhook webhook);

    Webhook update(String id, Webhook webhook);

    Webhook enable(String id);

    void delete(String id);

}
