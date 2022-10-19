package com.runsidekick.broker.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.repository.ApplicationConfigRepository;
import com.runsidekick.broker.util.ApplicationAwareProbeQueryFilter;
import com.runsidekick.broker.util.ProbeUtil;
import com.runsidekick.repository.BaseDBRepository;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Repository
public class ApplicationConfigRepositoryImpl extends BaseDBRepository implements ApplicationConfigRepository {

    private static final String TABLE_NAME = "ApplicationConfig";

    private RowMapper<ApplicationConfig> rowMapper;

    @PostConstruct
    public void init() {
        rowMapper = createApplicationConfigRowMapper(mapper);
    }

    private RowMapper<ApplicationConfig> createApplicationConfigRowMapper(ObjectMapper mapper) {
        return new BeanPropertyRowMapper<ApplicationConfig>(ApplicationConfig.class) {
            @SneakyThrows
            @Override
            protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) {
                if (pd.getName().equals("applicationFilter")) {
                    String applicationFiltersJson = rs.getString("application_filter");
                    if (!StringUtils.hasText(applicationFiltersJson)) {
                        return null;
                    }
                    return mapper.readValue(
                            applicationFiltersJson,
                            new TypeReference<ApplicationFilter>() { });
                } else if (pd.getName().equals("config")) {
                    String config = rs.getString("config");
                    if (!StringUtils.hasText(config)) {
                        return null;
                    }
                    return mapper.readValue(
                            config,
                            new TypeReference<Map<String, Object>>() { });
                } else {
                    return super.getColumnValue(rs, index, pd);
                }
            }
        };
    }

    @Override
    public ApplicationConfig getApplicationConfig(String workspaceId, ApplicationFilter applicationFilter) {
        ApplicationAwareProbeQueryFilter queryFilter = applicationFilterQuery(workspaceId, applicationFilter);

        List<ApplicationConfig> applicationConfigs =
                jdbcTemplate.query(
                        "SELECT * FROM " + TABLE_NAME + " WHERE workspace_id = ?" +
                                queryFilter.getFiltersExpr().toString(),
                        rowMapper,
                        queryFilter.getArgs().toArray());
        applicationConfigs = filterApplicationConfigs(applicationConfigs, applicationFilter);
        return applicationConfigs != null && applicationConfigs.size() > 0 ? applicationConfigs.get(0) : null;
    }

    @SneakyThrows
    @Override
    public void saveApplicationConfig(ApplicationConfig applicationConfig) {
        jdbcTemplate.update(
                "INSERT INTO " + TABLE_NAME +
                        "(id, workspace_id, application_filter, config)" +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "workspace_id = VALUES(workspace_id), " +
                        "application_filter = VALUES(application_filter)," +
                        "config = VALUES(config)",
                applicationConfig.getId(), applicationConfig.getWorkspaceId(),
                mapper.writeValueAsString(applicationConfig.getApplicationFilter()),
                mapper.writeValueAsString(applicationConfig.getConfig()));
    }

    @Override
    public void attachDetach(String id, boolean attach) {
        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME +
                        " SET detached = ?" +
                        " WHERE id = ?",
                attach ? 0 : 1,
                id);
    }

    private List<ApplicationConfig> filterApplicationConfigs(List<ApplicationConfig> applicationConfigs,
                                                             ApplicationFilter filter) {
        List<ApplicationConfig> filteredApplicationConfigs = new ArrayList<>();
        for (ApplicationConfig applicationConfig : applicationConfigs) {
            ApplicationFilter applicationFilter = applicationConfig.getApplicationFilter();
            if (ProbeUtil.isFiltered(filter, applicationFilter)) {
                filteredApplicationConfigs.add(applicationConfig);
                break;
            }
        }
        return filteredApplicationConfigs;

    }

    private ApplicationAwareProbeQueryFilter applicationFilterQuery(
            String workspaceId, ApplicationFilter applicationFilter) {
        StringBuilder filtersExpr = new StringBuilder();
        List args = new ArrayList<>();
        args.add(workspaceId);
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
