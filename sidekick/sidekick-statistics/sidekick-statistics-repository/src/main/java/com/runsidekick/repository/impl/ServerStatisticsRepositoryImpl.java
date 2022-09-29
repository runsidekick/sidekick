package com.runsidekick.repository.impl;

import com.runsidekick.model.ServerStatistics;
import com.runsidekick.repository.BaseDBRepository;
import com.runsidekick.repository.ServerStatisticsRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Service
public class ServerStatisticsRepositoryImpl extends BaseDBRepository implements ServerStatisticsRepository {

    private static final String TABLE_NAME = "ServerStatistics";
    private final RowMapper<ServerStatistics> rowMapper = new BeanPropertyRowMapper<>(ServerStatistics.class);

    @Override
    public boolean add(String workspaceId) {
        try {
            jdbcTemplate.update("INSERT INTO " + TABLE_NAME + " (workspace_id) VALUES (?)", workspaceId);
            return true;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }

    @Override
    public void increaseApplicationInstanceCount(String workspaceId) {
        increaseMetricCount(workspaceId, "application_instance_count");
    }

    @Override
    public void increaseTracePointCount(String workspaceId) {
        increaseMetricCount(workspaceId, "tracepoint_count");
    }

    @Override
    public void increaseLogPointCount(String workspaceId) {
        increaseMetricCount(workspaceId, "logpoint_count");
    }

    @Override
    public ServerStatistics getServerStatistics(String workspaceId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM " + TABLE_NAME + " WHERE workspace_id = ?",
                    rowMapper, workspaceId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ServerStatistics> getAllServerStatistics() {
        return jdbcTemplate.query("SELECT * FROM " + TABLE_NAME,
                rowMapper);
    }

    private void increaseMetricCount(String workspaceId, String metric) {
        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME +
                        " SET " + metric + " = " + metric + " + 1 " +
                        "WHERE workspace_id = ?",
                workspaceId
        );
    }

}
