package com.runsidekick.broker.model.request.impl;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public abstract class BaseApplicationAwareRequest extends BaseClientRequest {

    protected List<String> applications;
    protected boolean persist = false;
    // WorkspaceId for api-broker websocket connection
    protected String workspaceId;

    public void addApplication(String application) {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        applications.add(application);
    }

}
