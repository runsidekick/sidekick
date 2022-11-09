package com.runsidekick.broker.service;

import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ApplicationConfigService {

    ApplicationConfig getApplicationConfig(String workspaceId, ApplicationFilter applicationFilter);

    void saveApplicationConfig(ApplicationConfig applicationConfig);

    void attachApplication(String workspaceId, ApplicationFilter applicationFilter);

    void detachApplication(String workspaceId, ApplicationFilter applicationFilter);

    List<ApplicationConfig> listApplicationConfigs(String workspaceId);
}
