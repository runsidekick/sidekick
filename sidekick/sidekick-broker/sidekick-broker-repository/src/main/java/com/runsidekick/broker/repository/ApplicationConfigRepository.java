package com.runsidekick.broker.repository;

import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;

/**
 * @author yasin.kalafat
 */
public interface ApplicationConfigRepository {

    ApplicationConfig getApplicationConfig(String workspaceId, ApplicationFilter applicationFilter);

    void saveApplicationConfig(ApplicationConfig applicationConfig);

    void attachDetach(String id, boolean attach);

}
