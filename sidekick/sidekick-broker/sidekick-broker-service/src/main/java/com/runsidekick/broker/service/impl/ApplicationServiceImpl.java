package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.repository.ApplicationRepository;
import com.runsidekick.broker.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author serkan.ozal
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    @Cacheable(cacheNames = "Application", key = "#applicationInstanceId")
    public Application getApplication(String workspaceId, String applicationInstanceId) {
        return applicationRepository.getApplication(workspaceId, applicationInstanceId);
    }

    @Override
    @CacheEvict(cacheNames = "Application", key = "#application.instanceId")
    public void saveApplication(String workspaceId, Application application) {
        application.setWorkspaceId(workspaceId);
        applicationRepository.saveApplication(application);
    }

    @Override
    public void removeApplication(String workspaceId, String applicationInstanceId) {
        applicationRepository.removeApplication(workspaceId, applicationInstanceId);
    }

    @Override
    public Collection<Application> listApplications(String workspaceId) {
        return applicationRepository.listActiveApplications(workspaceId);
    }

    @Override
    public Set<String> filterApplications(String workspaceId, List<ApplicationFilter> applicationFilters) {
        return applicationRepository.filterApplications(workspaceId, applicationFilters);
    }

    @Override
    public Integer getApplicationCount(String workspaceId) {
        return applicationRepository.getApplicationCount(workspaceId);
    }

    @Override
    public List<Application> listApplications(String workspaceId, String client,
                                                    ListApplicationsRequest listApplicationsRequest) {
        Collection<Application> allApps = listApplications(workspaceId);
        List<Application> applicationList = new ArrayList<>();
        for (Application app : allApps) {
            boolean filtered = true;
            if (listApplicationsRequest.getApplicationNames() != null
                    && !listApplicationsRequest.getApplicationNames().isEmpty()) {
                if (!StringUtils.hasText(app.getName())) {
                    filtered &= false;
                } else {
                    filtered &= listApplicationsRequest.getApplicationNames().contains(app.getName());
                }
            }
            if (filtered && listApplicationsRequest.getApplicationStages() != null
                    && !listApplicationsRequest.getApplicationStages().isEmpty()) {
                if (!StringUtils.hasText(app.getStage())) {
                    filtered &= false;
                } else {
                    filtered &= listApplicationsRequest.getApplicationStages().contains(app.getStage());
                }
            }
            if (filtered && listApplicationsRequest.getApplicationVersions() != null
                    && !listApplicationsRequest.getApplicationVersions().isEmpty()) {
                if (!StringUtils.hasText(app.getVersion())) {
                    filtered &= false;
                } else {
                    filtered &= listApplicationsRequest.getApplicationVersions().contains(app.getVersion());
                }
            }
            if (filtered) {
                app.setTracePoints(filterTracePoints(app.getTracePoints(), client));
                app.setLogPoints(filterLogPoints(app.getLogPoints(), client));
                applicationList.add(app);
            }
        }
        return applicationList;
    }

    private List<TracePoint> filterTracePoints(List<TracePoint> tracePoints, String client) {
        if (tracePoints == null) {
            return null;
        }
        List<TracePoint> filteredTracePoints = new ArrayList<>(2 * tracePoints.size());
        for (TracePoint tracePoint : tracePoints) {
            if (client.equals(tracePoint.getClient())) {
                filteredTracePoints.add(tracePoint);
            }
        }
        return filteredTracePoints;
    }

    private List<LogPoint> filterLogPoints(List<LogPoint> logPoints, String client) {
        if (logPoints == null) {
            return null;
        }
        List<LogPoint> filteredLogPoints = new ArrayList<>(2 * logPoints.size());
        for (LogPoint logPoint : logPoints) {
            if (client.equals(logPoint.getClient())) {
                filteredLogPoints.add(logPoint);
            }
        }
        return filteredLogPoints;
    }

}
