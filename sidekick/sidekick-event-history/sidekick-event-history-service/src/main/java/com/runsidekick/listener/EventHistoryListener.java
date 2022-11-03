package com.runsidekick.listener;

import com.runsidekick.model.EventHistory;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryListener {

    default boolean isEventHistoryEnabled(EventHistory eventHistory) {
        return false;
    }
}
