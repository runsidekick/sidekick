package com.runsidekick.broker.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.repository.ApplicationRepository;
import com.runsidekick.repository.BaseDBRepository;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
@Repository
public class ApplicationRepositoryImpl extends BaseDBRepository implements ApplicationRepository {

    private RowMapper<Application> applicationRowMapper;

    @PostConstruct
    public void init() {
        applicationRowMapper = createApplicationRowMapper(mapper);
    }

    private RowMapper<Application> createApplicationRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<Application>(Application.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("tracePoints")) {
                    String tracePointsJson = rs.getString("trace_points");
                    if (StringUtils.isEmpty(tracePointsJson)) {
                        return null;
                    }
                    return mapper.readValue(tracePointsJson, new TypeReference<List<TracePoint>>() {
                    });
                } else if (pd.getName().equals("customTags")) {
                    String tagsJson = rs.getString("custom_tags");
                    if (StringUtils.isEmpty(tagsJson)) {
                        return null;
                    }
                    return mapper.readValue(tagsJson, new TypeReference<List<Application.CustomTag>>() {
                    });
                } else if (pd.getName().equals("logPoints")) {
                    String logPointsJson = rs.getString("log_points");
                    if (StringUtils.isEmpty(logPointsJson)) {
                        return null;
                    }
                    return mapper.readValue(logPointsJson, new TypeReference<List<LogPoint>>() {
                    });
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    private long getLastFiveMinute() {
        return System.currentTimeMillis() - FIVE_MINUTE;
    }

    @Override
    public Application getApplication(String workspaceId, String applicationInstanceId) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM Application WHERE workspace_id = ? AND instance_id = ? AND last_update_time > ?",
                applicationRowMapper,
                workspaceId, applicationInstanceId, getLastFiveMinute());
    }

    @SneakyThrows
    @Override
    public void saveApplication(Application application) {
        jdbcTemplate.update(
                "INSERT INTO " +
                        "Application(" +
                        "workspace_id, instance_id, name, " +
                        "version, stage, ip, " +
                        "host_name, runtime, last_update_time, " +
                        "trace_points, log_points, custom_tags) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "last_update_time = VALUES(last_update_time), " +
                        "trace_points = VALUES(trace_points)," +
                        "log_points = VALUES(log_points)",
                application.getWorkspaceId(), application.getInstanceId(), application.getName(),
                application.getVersion(), application.getStage(), application.getIp(),
                application.getHostName(), application.getRuntime(), System.currentTimeMillis(),
                mapper.writeValueAsString(application.getTracePoints()),
                mapper.writeValueAsString(application.getLogPoints()),
                mapper.writeValueAsString(application.getCustomTags()));
    }

    @Override
    public void removeApplication(String workspaceId, String applicationInstanceId) {
        jdbcTemplate.update(
                "DELETE FROM Application WHERE workspace_id = ? AND instance_id = ?",
                workspaceId, applicationInstanceId);
    }

    @Override
    public Collection<Application> listActiveApplications(String workspaceId) {
        return jdbcTemplate.query(
                "SELECT * FROM Application WHERE workspace_id = ? AND last_update_time > ?",
                applicationRowMapper,
                workspaceId, getLastFiveMinute());
    }

    @Override
    public Set<String> filterApplications(String workspaceId, List<ApplicationFilter> applicationFilters) {
        Set<String> apps = new HashSet<>();
        if (applicationFilters != null) {
            Collection<Application> applicationList = listActiveApplications(workspaceId);
            for (Application app : applicationList) {
                for (ApplicationFilter filter : applicationFilters) {
                    boolean filtered = true;
                    if (filter.getName() != null) {
                        if (StringUtils.isEmpty(app.getName())) {
                            filtered = false;
                        } else {
                            filtered = filter.getName().equals(app.getName());
                        }
                    }
                    if (filtered && filter.getStage() != null && !StringUtils.isEmpty(app.getStage())) {
                        filtered = filter.getStage().equals(app.getStage());
                    }
                    if (filtered && filter.getVersion() != null && !StringUtils.isEmpty(app.getVersion())) {
                        filtered = filter.getVersion().equals(app.getVersion());
                    }
                    if (filtered && filter.getCustomTags() != null && !filter.getCustomTags().isEmpty()) {
                        Map<String, String> customTagMap = app.getCustomTags().stream()
                                .collect(Collectors.toMap(
                                        Application.CustomTag::getTagName,
                                        Application.CustomTag::getTagValue, (a, b) -> b));
                        for (Map.Entry<String, String> entry :
                                filter.getCustomTags().entrySet()) {
                            if (customTagMap.containsKey(entry.getKey())) {
                                String tagValue = customTagMap.get(entry.getKey());
                                if (!(entry.getValue() != null && entry.getValue().equals(tagValue))) {
                                    filtered = false;
                                    break;
                                }
                            } else {
                                filtered = false;
                                break;
                            }
                        }
                    }
                    if (filtered) {
                        apps.add(app.getInstanceId());
                        break;
                    }
                }
            }
        }
        return apps;
    }

    @Override
    public Integer getApplicationCount(String workspaceId) {
        String sql = "SELECT COUNT(1) FROM Application WHERE workspace_id = ? AND last_update_time > ?";
        Object[] args = {workspaceId, getLastFiveMinute()};
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

}
