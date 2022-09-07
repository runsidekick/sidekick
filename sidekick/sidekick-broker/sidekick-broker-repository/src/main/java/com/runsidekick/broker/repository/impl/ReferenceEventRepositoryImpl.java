package com.runsidekick.broker.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.repository.ReferenceEventRepository;
import com.runsidekick.broker.util.ApplicationAwareProbeQueryFilter;
import com.runsidekick.repository.BaseDBRepository;
import lombok.SneakyThrows;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Repository
public class ReferenceEventRepositoryImpl extends BaseDBRepository implements ReferenceEventRepository {

    private static final String TABLE_NAME = "ReferenceEvent";

    private RowMapper<ReferenceEvent> rowMapper;

    @PostConstruct
    public void init() {
        rowMapper = createReferenceEventRowMapper(mapper);
    }

    private RowMapper<ReferenceEvent> createReferenceEventRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<ReferenceEvent>(ReferenceEvent.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("applicationFilter")) {
                    String applicationFilterJson = rs.getString("application_filter");
                    if (StringUtils.isEmpty(applicationFilterJson)) {
                        return null;
                    }
                    return mapper.readValue(
                            applicationFilterJson,
                            new TypeReference<ApplicationFilter>() { });
                } else if (pd.getName().equals("probeType")) {
                    return ProbeType.valueOf(rs.getString("probe_type"));
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    @Override
    public ReferenceEvent get(String workspaceId, String probeId, ProbeType probeType,
                              ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter = applicationQueryFilter(applicationFilter);
        List<String> args = new ArrayList<>();
        args.add(workspaceId);
        args.add(probeId);
        args.add(probeType.name());
        args.addAll(queryFilter.getArgs());

        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE workspace_id = ? AND probe_id = ? AND probe_type = ?" +
                queryFilter.getFiltersExpr().toString();

        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, args.toArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(ReferenceEvent referenceEvent) throws Exception {
        delete(referenceEvent.getWorkspaceId(), referenceEvent.getProbeId(), referenceEvent.getProbeType(),
                referenceEvent.getApplicationFilter());

        jdbcTemplate.update(
                "INSERT INTO " + TABLE_NAME +
                        "(workspace_id, probe_id, probe_type, application_filter, event) " +
                        "VALUES (?, ?, ?, ?, ?) ",
                referenceEvent.getWorkspaceId(), referenceEvent.getProbeId(), referenceEvent.getProbeType().name(),
                mapper.writeValueAsString(referenceEvent.getApplicationFilter()), referenceEvent.getEvent());
    }

    @Override
    public void delete(String workspaceId, String probeId, ProbeType probeType, ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter = applicationQueryFilter(applicationFilter);

        List<String> args = new ArrayList<>();
        args.add(workspaceId);
        args.add(probeId);
        args.add(probeType.name());
        args.addAll(queryFilter.getArgs());

        String sql = "DELETE FROM " + TABLE_NAME + " WHERE workspace_id = ? AND probe_id = ? AND probe_type = ? " +
                queryFilter.getFiltersExpr().toString();

        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void delete(String workspaceId, String probeId, ProbeType probeType) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE workspace_id = ? AND probe_id = ? AND probe_type = ?";
        Object[] args = {workspaceId, probeId, probeType.name()};

        jdbcTemplate.update(sql, args);
    }

    @Override
    public void delete(String workspaceId, List<String> probeIds, ProbeType probeType,
                       ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter = applicationQueryFilter(applicationFilter);

        List<Object> args = new ArrayList<>();
        args.add(workspaceId);
        args.add(probeIds);
        args.add(probeType.name());
        args.addAll(queryFilter.getArgs());

        String sql = "DELETE FROM " + TABLE_NAME +
                " WHERE workspace_id = ? AND probe_id IN (?) AND probe_type = ? " +
                queryFilter.getFiltersExpr().toString();

        jdbcTemplate.update(sql, args);
    }

    @Override
    public void delete(String workspaceId, List<String> probeIds, ProbeType probeType) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE workspace_id = ? AND probe_id IN (?) AND probe_type = ?";
        Object[] args = {workspaceId, probeIds.toArray(), probeType.name()};

        jdbcTemplate.update(sql, args);

    }

    private ApplicationAwareProbeQueryFilter applicationQueryFilter(ApplicationFilter applicationFilter) {
        StringBuilder filtersExpr = new StringBuilder();
        List<String> args = new ArrayList<>();
        if (StringUtils.hasText(applicationFilter.getName())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.name') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.name') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getName());
        }
        if (StringUtils.hasText(applicationFilter.getStage())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.stage') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.stage') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getStage());
        }
        if (StringUtils.hasText(applicationFilter.getVersion())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.version') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filter , '$.version') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getVersion());
        }
        return new ApplicationAwareProbeQueryFilter(filtersExpr, args);
    }
}
