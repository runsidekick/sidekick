package com.runsidekick.broker.proxy.listener.impl;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.event.impl.ApplicationDisconnectEvent;
import com.runsidekick.broker.model.event.impl.ApplicationStatusEvent;
import com.runsidekick.broker.model.event.impl.BaseEvent;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.Communicator;
import com.runsidekick.broker.proxy.listener.SessionCallback;
import com.runsidekick.broker.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionCallbackImpl implements SessionCallback {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Communicator communicator;

    @Override
    public void onApplicationSessionAdd(ChannelInfo channelInfo) {
        ApplicationMetadata applicationMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();
        if (applicationMetadata != null) {
            Application application = new Application();
            application.setInstanceId(applicationMetadata.getInstanceId());
            application.setName(applicationMetadata.getName());
            application.setStage(applicationMetadata.getStage());
            application.setVersion(applicationMetadata.getVersion());
            application.setIp(applicationMetadata.getIp());
            application.setHostName(applicationMetadata.getHostName());
            application.setRuntime(applicationMetadata.getRuntime());
            application.setCustomTags(applicationMetadata.getCustomTags());

            // Save new application
            applicationService.saveApplication(channelInfo.getWorkspaceId(), application);

            ApplicationStatusEvent applicationStatusEvent =
                    initEvent(new ApplicationStatusEvent(application), applicationMetadata);
            // Notify clients about new joined app
            communicator.sendMessageToClients(channelInfo, applicationStatusEvent);

        }
    }

    @Override
    public void onClientSessionAdd(ChannelInfo channelInfo) {
    }

    @Override
    public void onApiSessionAdd(ChannelInfo channelInfo) {
    }

    @Override
    public void onApplicationSessionRemove(ChannelInfo channelInfo) {
        ApplicationMetadata applicationMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();
        if (applicationMetadata != null) {
            applicationService.removeApplication(
                    channelInfo.getWorkspaceId(), applicationMetadata.getInstanceId());

            ApplicationDisconnectEvent applicationDisconnectEvent =
                    initEvent(new ApplicationDisconnectEvent(), applicationMetadata);
            // Notify clients about disconnection of app
            communicator.sendMessageToClients(channelInfo, applicationDisconnectEvent);
        }
    }

    @Override
    public void onClientSessionRemove(ChannelInfo channelInfo) {
    }

    @Override
    public void onApiSessionRemove(ChannelInfo channelInfo) {
    }

    private <E extends BaseEvent> E initEvent(E event, ApplicationMetadata applicationMetadata) {
        event.setId(UUID.randomUUID().toString());
        event.setTime(System.currentTimeMillis());
        event.setHostName(applicationMetadata.getHostName());
        event.setApplicationName(applicationMetadata.getName());
        event.setApplicationInstanceId(applicationMetadata.getInstanceId());
        return event;
    }

}
