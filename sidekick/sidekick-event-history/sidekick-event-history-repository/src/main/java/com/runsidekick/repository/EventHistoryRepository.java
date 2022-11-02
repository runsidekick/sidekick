package com.runsidekick.repository;

import com.runsidekick.model.EventHistory;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryRepository {
    void save(EventHistory eventHistory);
}
