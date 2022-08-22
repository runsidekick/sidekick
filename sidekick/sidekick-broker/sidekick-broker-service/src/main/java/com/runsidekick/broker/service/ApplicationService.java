package com.runsidekick.broker.service;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author serkan.ozal
 */
@Service
public interface ApplicationService {

    Application getApplication(String workspaceId, String applicationInstanceId);
    void saveApplication(String workspaceId, Application application);
    void removeApplication(String workspaceId, String applicationInstanceId);
    Collection<Application> listApplications(String workspaceId);
    Set<String> filterApplications(String workspaceId, List<ApplicationFilter> applicationFilters);
    Integer getApplicationCount(String workspaceId);
    List<Application> listApplications(String workspaceId, String client,
                                             ListApplicationsRequest listApplicationsRequest);
}
