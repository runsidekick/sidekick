package com.runsidekick.model.util;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.model.request.EventHistoryRequest;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class EventHistoryQueryFilter {

    private StringBuilder filtersExpr;
    private List<Object> args;

    public EventHistoryQueryFilter() {
        filtersExpr = new StringBuilder();
        args = new ArrayList<>();
    }

    public EventHistoryQueryFilter prepareFilter(EventHistoryRequest request) {
        boolean hasFilter = false;
        if (StringUtils.hasText(request.getWorkspaceId())) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("workspace_id = ?");
            args.add(request.getWorkspaceId());
            hasFilter = true;
        }
        if (StringUtils.hasText(request.getFileName())) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("file_name = ?");
            args.add(request.getFileName());
            hasFilter = true;
        }
        if (request.getLineNo() > 0) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("line_no = ?");
            args.add(request.getLineNo());
            hasFilter = true;
        }
        if (StringUtils.hasText(request.getClient())) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("client = ?");
            args.add(request.getClient());
            hasFilter = true;
        }
        if (StringUtils.hasText(request.getProbeName())) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("probe_name = ?");
            args.add(request.getProbeName());
            hasFilter = true;
        }
        if (request.getType() != null) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("type = ?");
            args.add(request.getType().name());
            hasFilter = true;
        }
        if (request.getStartDate() != null) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("created_at >= ?");
            args.add(request.getStartDate());
            hasFilter = true;
        }
        if (request.getEndDate() != null) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("created_at <= ?");
            args.add(request.getEndDate());
            hasFilter = true;
        }

        if (StringUtils.hasText(request.getTag())) {
            filtersExpr.append(hasFilter ? " AND " : " WHERE ");
            filtersExpr.append("JSON_SEARCH(probe_tags , 'all', ? , NULL) IS NOT NULL");
            args.add(request.getTag());
            hasFilter = true;
        }

        if (request.getApplicationFilter() != null) {
            ApplicationFilter applicationFilter = request.getApplicationFilter();
            if (StringUtils.hasText(applicationFilter.getName())) {
                filtersExpr.append(hasFilter ? " AND " : " WHERE ");
                filtersExpr.append("(");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.name') = ?");
                filtersExpr.append(" OR ");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.name') IS NULL");
                filtersExpr.append(")");
                args.add(applicationFilter.getName());
                hasFilter = true;
            }
            if (StringUtils.hasText(applicationFilter.getStage())) {
                filtersExpr.append(hasFilter ? " AND " : " WHERE ");
                filtersExpr.append("(");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.stage') = ?");
                filtersExpr.append(" OR ");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.stage') IS NULL");
                filtersExpr.append(")");
                args.add(applicationFilter.getStage());
                hasFilter = true;
            }
            if (StringUtils.hasText(applicationFilter.getVersion())) {
                filtersExpr.append(hasFilter ? " AND " : " WHERE ");
                filtersExpr.append("(");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.version') = ?");
                filtersExpr.append(" OR ");
                filtersExpr.append("JSON_EXTRACT(application_filter , '$.version') IS NULL");
                filtersExpr.append(")");
                args.add(applicationFilter.getVersion());
                hasFilter = true;
            }
        }
        return this;
    }
}