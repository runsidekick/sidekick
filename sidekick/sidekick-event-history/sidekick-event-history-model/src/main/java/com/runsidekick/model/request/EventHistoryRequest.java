package com.runsidekick.model.request;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryRequest {
    private String workspaceId;
    private EventType type;
    private ApplicationFilter applicationFilter;
    private String fileName;
    private int lineNo;
    private String client;
    private String probeName;
    private String tag;
    private boolean withEventData;
    private Date startDate;
    private Date endDate;
    private GroupBy groupBy;

    public enum GroupBy {
        DAILY, HOURLY
    }
}
