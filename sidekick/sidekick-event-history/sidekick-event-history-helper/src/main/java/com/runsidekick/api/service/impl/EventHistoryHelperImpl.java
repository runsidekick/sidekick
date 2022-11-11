package com.runsidekick.api.service.impl;

import com.runsidekick.helper.EventHistoryHelper;
import com.runsidekick.model.EventHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class EventHistoryHelperImpl implements EventHistoryHelper {

    @Value("${eventhistory.enabled:false}")
    private boolean eventHistoryEnabled;

    @Override
    public boolean isEventHistoryEnabled(EventHistory eventHistory) {
        return eventHistoryEnabled;
    }
}
