package com.runsidekick.repository.impl;

import com.runsidekick.model.Webhook;
import com.runsidekick.model.WebhookType;
import com.runsidekick.repository.BaseDBRepository;
import com.runsidekick.repository.WebhookRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Repository
public class WebhookRepositoryImpl extends BaseDBRepository implements WebhookRepository {

    private static final String TABLE_NAME = "Webhook";

    private Webhook getWebhook(ResultSet resultSet) throws SQLException {
        Webhook entity = new Webhook();
        entity.setId(resultSet.getString("id"));
        entity.setWorkspaceId(resultSet.getString("workspace_id"));
        entity.setName(resultSet.getString("name"));
        entity.setName(resultSet.getString("name"));
        entity.setType(WebhookType.toWebhookType(resultSet.getString("type")));
        entity.setConfig(resultSet.getString("config"));
        entity.setDisabled(resultSet.getBoolean("disabled"));
        entity.setLastErrorReason(resultSet.getString("last_error_reason"));
        Timestamp lastErrorDate = resultSet.getTimestamp("last_error_date");
        if (lastErrorDate != null) {
            entity.setLastErrorDate(lastErrorDate.toLocalDateTime());
        }
        return entity;
    }

    private final RowMapper<Webhook> rowMapper = (resultSet, i) -> getWebhook(resultSet);

    @Override
    public List<Webhook> listByWorkspaceId(String workspaceId) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE workspace_id = ? ";
        Object[] args = {workspaceId};

        return jdbcTemplate.query(sql, rowMapper, args);
    }

    @Override
    public void save(Webhook webhook) {
        String sql = "INSERT INTO " + TABLE_NAME
                + "(id,workspace_id,name,type,config,disabled,last_error_reason,last_error_date) " +
                "VALUES (:id,:workspaceId,:name,:type,:config,:disabled,:lastErrorReason,:lastErrorDate) " +
                "ON DUPLICATE KEY UPDATE " +
                "workspace_id=VALUES(workspace_id), name=VALUES(name), type=VALUES(type), " +
                "config=VALUES(config), disabled=VALUES(disabled), " +
                "last_error_reason=VALUES(last_error_reason), last_error_date=VALUES(last_error_date)";

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(webhook);
        parameterSource.registerSqlType("type", Types.VARCHAR);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id=?";
        Object[] args = {id};
        jdbcTemplate.update(sql, args);
    }

    @Override
    public Webhook findById(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id=?";
        Object[] args = {id};
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
