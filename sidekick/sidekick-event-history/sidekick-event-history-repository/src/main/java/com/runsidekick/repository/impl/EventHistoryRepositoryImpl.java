package com.runsidekick.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.model.EventHistory;
import com.runsidekick.model.EventType;
import com.runsidekick.model.request.EventHistoryRequest;
import com.runsidekick.model.util.EventHistoryQueryFilter;
import com.runsidekick.repository.BaseDBRepository;
import com.runsidekick.repository.EventHistoryRepository;
import lombok.SneakyThrows;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Repository
public class EventHistoryRepositoryImpl extends BaseDBRepository implements EventHistoryRepository {

    private static final String TABLE_NAME = "EventHistory";

    private RowMapper<EventHistory> eventHistoryRowMapper;

    @PostConstruct
    public void init() {
        eventHistoryRowMapper = createEventHistoryRowMapper(mapper);
    }

    private RowMapper<EventHistory> createEventHistoryRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<EventHistory>(EventHistory.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("applicationFilter")) {
                    String applicationFilterJson = rs.getString("application_filter");
                    if (!StringUtils.hasText(applicationFilterJson)) {
                        return null;
                    }
                    return mapper.readValue(
                            applicationFilterJson,
                            new TypeReference<ApplicationFilter>() {
                            });
                } else if (pd.getName().equals("probeTags")) {
                    String tags = rs.getString("probe_tags");
                    if (!StringUtils.hasText(tags)) {
                        return null;
                    }
                    return mapper.readValue(
                            tags,
                            new TypeReference<List<String>>() {
                            });
                } else if (pd.getName().equals("type")) {
                    String type = rs.getString("type");
                    if (!StringUtils.hasText(type)) {
                        return null;
                    }
                    return EventType.toEventType(type);
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    @Override
    public void save(EventHistory eventHistory) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO " + TABLE_NAME +
                            "(id, workspace_id, type, " +
                            "file_name, line_no, client, " +
                            "event_data, probe_name, " +
                            "application_filter, probe_tags) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ",
                    eventHistory.getId(), eventHistory.getWorkspaceId(), eventHistory.getType().toString(),
                    eventHistory.getFileName(), eventHistory.getLineNo(), eventHistory.getClient(),
                    eventHistory.getEventData(), eventHistory.getProbeName(),
                    mapper.writeValueAsString(eventHistory.getApplicationFilter()),
                    mapper.writeValueAsString(eventHistory.getProbeTags()));
        } catch (Exception e) {
        }
    }

    @Override
    public List<EventHistory> queryEventHistory(EventHistoryRequest request, int page, int size) {
        EventHistoryQueryFilter eventHistoryQueryFilter = new EventHistoryQueryFilter().prepareFilter(request);
        String sql = "SELECT * FROM " + TABLE_NAME + eventHistoryQueryFilter.getFiltersExpr().toString() +
                " ORDER BY created_at desc " + pagination(page, size);
        try {
            return jdbcTemplate.query(sql, eventHistoryRowMapper, eventHistoryQueryFilter.getArgs().toArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}