package com.runsidekick.broker.repository;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author serkan.ozal
 */
public interface ApplicationRepository {

    Application getApplication(String workspaceId, String applicationInstanceId);

    void saveApplication(Application application);

    void removeApplication(String workspaceId, String applicationInstanceId);

    Collection<Application> listActiveApplications(String workspaceId);

    Set<String> filterApplications(String workspaceId, List<ApplicationFilter> applicationFilters);

    Integer getApplicationCount(String workspaceId);
}
