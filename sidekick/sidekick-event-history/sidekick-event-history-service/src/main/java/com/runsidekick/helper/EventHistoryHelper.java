package com.runsidekick.helper;

import com.runsidekick.model.EventHistory;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryHelper {

    default boolean isEventHistoryEnabled(EventHistory eventHistory) {
        return false;
    }
}
