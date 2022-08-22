package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.event.impl.ApplicationStatusEvent;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.stereotype.Component;

/**
 * @author serkan.ozal
 */
@Component
public class ApplicationStatusEventHandler
        extends BaseEventHandler<ApplicationStatusEvent> {

    public static final String EVENT_NAME = "ApplicationStatusEvent";

    public ApplicationStatusEventHandler() {
        super(EVENT_NAME, ApplicationStatusEvent.class);
    }

    @Override
    public EventAck handleEvent(ChannelInfo channelInfo, ApplicationStatusEvent event, EventContext context) {
        Application application = event.getApplication();
        ApplicationMetadata applicationMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();
        if (application.getIp() == null) {
            application.setIp(applicationMetadata.getIp());
        }
        application.setCustomTags(applicationMetadata.getCustomTags());
        context.setEventUpdated(true);
        applicationService.saveApplication(channelInfo.getWorkspaceId(), application);
        return null; // Let event handling mechanism does default behaviour
    }

}
