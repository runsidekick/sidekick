package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.repository.ApplicationConfigRepository;
import com.runsidekick.broker.service.ApplicationConfigService;
import io.thundra.swark.utils.UUIDUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author yasin.kalafat
 */
@Service
@RequiredArgsConstructor
public class ApplicationConfigServiceImpl implements ApplicationConfigService {

    private final ApplicationConfigRepository applicationConfigRepository;

    @Override
    public ApplicationConfig getApplicationConfig(String workspaceId, ApplicationFilter applicationFilter) {
        return applicationConfigRepository.getApplicationConfig(workspaceId, applicationFilter);
    }

    @Override
    public void saveApplicationConfig(ApplicationConfig applicationConfig) {
        ApplicationConfig existingApplicationConfig =
                applicationConfigRepository.getApplicationConfig(applicationConfig.getWorkspaceId(),
                        applicationConfig.getApplicationFilter());
        if (existingApplicationConfig != null) {
            applicationConfig.setId(existingApplicationConfig.getId());
        } else {
            applicationConfig.setId(UUIDUtils.generateId());
        }
        applicationConfigRepository.saveApplicationConfig(applicationConfig);
    }

    @Override
    public void attachApplication(String workspaceId, ApplicationFilter applicationFilter) {
        ApplicationConfig applicationConfig = getOrCreateApplicationConfig(workspaceId, applicationFilter);
        applicationConfigRepository.attachDetach(applicationConfig.getId(), true);
    }

    @Override
    public void detachApplication(String workspaceId, ApplicationFilter applicationFilter) {
        ApplicationConfig applicationConfig = getOrCreateApplicationConfig(workspaceId, applicationFilter);
        applicationConfigRepository.attachDetach(applicationConfig.getId(), false);
    }

    private ApplicationConfig getOrCreateApplicationConfig(String workspaceId, ApplicationFilter applicationFilter) {
        ApplicationConfig applicationConfig =
                applicationConfigRepository.getApplicationConfig(workspaceId, applicationFilter);
        if (applicationConfig != null) {
            return applicationConfig;
        }
        applicationConfig = ApplicationConfig.builder()
                .id(UUIDUtils.generateId())
                .workspaceId(workspaceId)
                .applicationFilter(applicationFilter)
                .config(Collections.emptyMap())
                .build();
        applicationConfigRepository.saveApplicationConfig(applicationConfig);
        return applicationConfig;
    }
}
