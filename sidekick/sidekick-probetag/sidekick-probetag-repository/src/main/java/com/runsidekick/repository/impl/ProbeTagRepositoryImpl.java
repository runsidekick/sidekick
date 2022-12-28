package com.runsidekick.repository.impl;

import com.runsidekick.model.ProbeTag;
import com.runsidekick.repository.BaseDBRepository;
import com.runsidekick.repository.ProbeTagRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Repository
public class ProbeTagRepositoryImpl extends BaseDBRepository implements ProbeTagRepository {

    private static final String TABLE_NAME = "ProbeTag";

    private final RowMapper<ProbeTag> rowMapper = new BeanPropertyRowMapper<>(ProbeTag.class);

    @Override
    public ProbeTag findById(String id) {
        try {
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id=?";
            Object[] args = {id};
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public ProbeTag getByWorkspaceId(String workspaceId, String tag) {
        try {
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE workspace_id = ? AND tag = ?";
            Object[] args = {workspaceId, tag};
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ProbeTag> listByWorkspaceId(String workspaceId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE workspace_id = ? ";
        Object[] args = {workspaceId};

        return jdbcTemplate.query(sql, rowMapper, args);
    }

    @Override
    public void save(ProbeTag probeTag) {
        try {
            String sql = "INSERT INTO " + TABLE_NAME
                    + "(id, workspace_id, tag) " +
                    "VALUES (:id, :workspaceId, :tag)";

            BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(probeTag);
            namedParameterJdbcTemplate.update(sql, parameterSource);
        } catch (DuplicateKeyException e) {

        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id=?";
        Object[] args = {id};
        jdbcTemplate.update(sql, args);
    }

    @Override
    public void disable(String workspaceId, String tag) {
        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME +
                        " SET disabled = 1 " +
                        "WHERE workspace_id = ? AND tag = ?",
                 workspaceId, tag
        );
    }

    @Override
    public void enable(String workspaceId, String tag) {
        jdbcTemplate.update(
                "UPDATE " + TABLE_NAME +
                        " SET disabled = 0 " +
                        "WHERE workspace_id = ? AND tag = ?",
                workspaceId, tag
        );
    }
}
