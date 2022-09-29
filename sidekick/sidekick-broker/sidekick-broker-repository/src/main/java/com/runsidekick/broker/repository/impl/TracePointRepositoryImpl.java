package com.runsidekick.broker.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.error.CodedException;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.repository.TracePointRepository;
import com.runsidekick.broker.util.ApplicationAwareProbeQueryFilter;
import com.runsidekick.broker.util.ProbeUtil;
import com.runsidekick.repository.BaseDBRepository;
import lombok.SneakyThrows;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.runsidekick.broker.error.ErrorCodes.TRACEPOINT_ALREADY_EXIST;
import static com.runsidekick.broker.util.TracePointUtil.getExpireCount;
import static com.runsidekick.broker.util.TracePointUtil.getExpireSecs;
import static com.runsidekick.broker.util.TracePointUtil.getExpireTimestamp;

/**
 * @author ozge.lule
 */
@Repository
public class TracePointRepositoryImpl extends BaseDBRepository implements TracePointRepository {

    private RowMapper<TracePoint> tracePointRowMapper;
    private RowMapper<TracePointConfig> tracePointConfigRowMapper;

    @PostConstruct
    public void init() {
        tracePointRowMapper = createTracePointRowMapper(mapper);
        tracePointConfigRowMapper = createTracePointConfigRowMapper(mapper);
    }

    private RowMapper<TracePointConfig> createTracePointConfigRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<TracePointConfig>(TracePointConfig.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("applicationFilters")) {
                    String applicationFiltersJson = rs.getString("application_filters");
                    if (StringUtils.isEmpty(applicationFiltersJson)) {
                        return null;
                    }
                    return mapper.readValue(
                            applicationFiltersJson,
                            new TypeReference<List<ApplicationFilter>>() { });
                } else if (pd.getName().equals("webhookIds")) {
                    String webhookIds = rs.getString("webhook_ids");
                    if (StringUtils.isEmpty(webhookIds)) {
                        return null;
                    }
                    return mapper.readValue(
                            webhookIds,
                            new TypeReference<List<String>>() { });
                } else if (pd.getName().equals("tags")) {
                    String tags = rs.getString("tags");
                    if (StringUtils.isEmpty(tags)) {
                        return null;
                    }
                    return mapper.readValue(
                            tags,
                            new TypeReference<List<String>>() { });
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    private RowMapper<TracePoint> createTracePointRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<TracePoint>(TracePoint.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("webhookIds")) {
                    String webhookIds = rs.getString("webhook_ids");
                    if (StringUtils.isEmpty(webhookIds)) {
                        return null;
                    }
                    return mapper.readValue(
                            webhookIds,
                            new TypeReference<List<String>>() { });
                } else if (pd.getName().equals("tags")) {
                    String tags = rs.getString("tags");
                    if (StringUtils.isEmpty(tags)) {
                        return null;
                    }
                    return mapper.readValue(
                            tags,
                            new TypeReference<List<String>>() { });
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    @Override
    public TracePointConfig getTracePoint(String workspaceId, String tracePointId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM TracePoint WHERE workspace_id = ? AND id = ?",
                    tracePointConfigRowMapper,
                    workspaceId, tracePointId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void putTracePoint(String workspaceId, String userId, TracePointConfig tracePointConfig, boolean fromApi)
            throws Exception {
        try {
            jdbcTemplate.update(
                    "INSERT INTO " +
                            "TracePoint(" +
                            "id, workspace_id, user_id, " +
                            "file_name, line_no, client, " +
                            "condition_expression, expire_secs, expire_count, " +
                            "tracing_enabled, file_hash, disabled, " +
                            "expire_timestamp, application_filters, webhook_ids, from_api, predefined, " +
                            "probe_name, tags) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ",
                    tracePointConfig.getId(), workspaceId, userId,
                    tracePointConfig.getFileName(), tracePointConfig.getLineNo(), tracePointConfig.getClient(),
                    tracePointConfig.getConditionExpression(),
                    getExpireSecs(tracePointConfig.getExpireSecs()),
                    getExpireCount(tracePointConfig.getExpireCount()),
                    tracePointConfig.isTracingEnabled(), tracePointConfig.getFileHash(), tracePointConfig.isDisabled(),
                    getExpireTimestamp(tracePointConfig.getExpireSecs()),
                    mapper.writeValueAsString(tracePointConfig.getApplicationFilters()),
                    mapper.writeValueAsString(tracePointConfig.getWebhookIds()),
                    fromApi,
                    tracePointConfig.isPredefined(), tracePointConfig.getProbeName(),
                    mapper.writeValueAsString(tracePointConfig.getTags()));
        } catch (DuplicateKeyException e) {
            throw new CodedException(
                    TRACEPOINT_ALREADY_EXIST,
                    tracePointConfig.getFileName(), tracePointConfig.getLineNo(), tracePointConfig.getClient());
        }
    }

    @Override
    public void removeTracePoint(String workspaceId, String userId, String tracePointId) {
        jdbcTemplate.update(
                "DELETE FROM TracePoint WHERE workspace_id = ? AND user_id = ? AND id = ?",
                workspaceId, userId, tracePointId);
    }

    @Override
    public long removeTracePoints(String workspaceId, String userId, List<String> tracePointIds) {
        return jdbcTemplate.update(
                "DELETE FROM TracePoint WHERE workspace_id = ? AND user_id = ? AND id IN (?)",
                workspaceId, userId, tracePointIds.toArray());
    }

    @Override
    public void enableDisableTracePoint(String workspaceId, String userId, String tracePointId, boolean disable) {
        jdbcTemplate.update(
                "UPDATE TracePoint " +
                        "SET disabled=? " +
                        "WHERE workspace_id = ? AND user_id = ? AND id = ?",
                disable, workspaceId, userId, tracePointId);
    }

    @Override
    @SneakyThrows
    public void updateTracePoint(String workspaceId, String userId, String tracePointId, TracePoint tracePoint) {
        jdbcTemplate.update(
                "UPDATE TracePoint " +
                        "SET " +
                            "condition_expression = ?, expire_secs = ?, expire_count = ?, " +
                            "expire_timestamp = ?, tracing_enabled = ?, disabled = ?, webhook_ids = ?, " +
                            "predefined = ?, probe_name = ?, tags = ?" +
                        "WHERE workspace_id = ? AND user_id = ? AND id = ?",
                tracePoint.getConditionExpression(),
                getExpireSecs(tracePoint.getExpireSecs()), getExpireCount(tracePoint.getExpireCount()),
                getExpireTimestamp(tracePoint.getExpireSecs()), tracePoint.isTracingEnabled(), tracePoint.isDisabled(),
                mapper.writeValueAsString(tracePoint.getWebhookIds()),
                tracePoint.isPredefined(), tracePoint.getProbeName(),
                mapper.writeValueAsString(tracePoint.getTags()),
                workspaceId, userId, tracePointId);
    }

    @Override
    public List<TracePoint> listTracePoints(String workspaceId, String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM TracePoint WHERE workspace_id = ? AND user_id = ?",
                tracePointRowMapper,
                workspaceId, userId);
    }

    @Override
    public Collection<TracePoint> queryTracePoints(String workspaceId, ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter = ProbeUtil.probeQueryFilter(workspaceId, applicationFilter);

        Collection<TracePointConfig> tracePointConfigs =
                jdbcTemplate.query(
                "SELECT * FROM TracePoint WHERE workspace_id = ?" + queryFilter.getFiltersExpr().toString(),
                    tracePointConfigRowMapper,
                        queryFilter.getArgs().toArray());
        return filterTracePoints(tracePointConfigs, applicationFilter);
    }

    @Override
    public List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM TracePoint WHERE workspace_id = ? AND user_id = ? AND predefined = 1",
                tracePointRowMapper,
                workspaceId, userId);
    }

    @Override
    public TracePoint queryTracePoint(String workspaceId, String tracepointId, ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter =
                ProbeUtil.probeQueryFilter(workspaceId, tracepointId, applicationFilter);

        Collection<TracePointConfig> tracePointConfigs =
                jdbcTemplate.query(
                        "SELECT * FROM TracePoint WHERE workspace_id = ? AND id = ?" +
                                queryFilter.getFiltersExpr().toString(),
                        tracePointConfigRowMapper,
                        queryFilter.getArgs().toArray());
        List<TracePoint> tracePoints = filterTracePoints(tracePointConfigs, applicationFilter);
        return tracePoints != null && tracePoints.size() > 0 ? tracePoints.get(0) : null;
    }

    private List<TracePoint> filterTracePoints(Collection<TracePointConfig> tracePointConfigs,
                                                 ApplicationFilter filter) {
        Collection<TracePointConfig> filteredTracePoints = ProbeUtil.filterProbes(tracePointConfigs, filter);
        return filteredTracePoints.stream().collect(Collectors.toList());
    }

}
