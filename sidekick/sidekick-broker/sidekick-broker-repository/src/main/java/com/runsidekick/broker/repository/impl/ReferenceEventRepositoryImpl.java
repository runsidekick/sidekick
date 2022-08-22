package com.runsidekick.broker.repository.impl;

import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.repository.ReferenceEventRepository;
import com.runsidekick.repository.BaseDBRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Repository
public class ReferenceEventRepositoryImpl extends BaseDBRepository implements ReferenceEventRepository {

    private static final String TABLE_NAME = "ReferenceEvent";
    private final RowMapper<ReferenceEvent> rowMapper = (resultSet, i) -> ReferenceEvent.builder()
            .probeId(resultSet.getString("probe_id"))
            .probeType(ProbeType.valueOf(resultSet.getString("probe_type")))
            .event(resultSet.getString("event"))
            .build();

    @Override
    public ReferenceEvent get(String probeId, ProbeType probeType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE probe_id = ? AND probe_type = ?";
        Object[] args = {probeId, probeType.name()};

        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(ReferenceEvent referenceEvent) {
        delete(referenceEvent.getProbeId(), referenceEvent.getProbeType());

        String sql = "INSERT INTO " + TABLE_NAME
                + "(probe_id, probe_type, event) " +
                "VALUES (:probeId, :probeType, :event)";

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(referenceEvent);
        parameterSource.registerSqlType("probeType", Types.VARCHAR);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public void delete(String probeId, ProbeType probeType) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE probe_id = ? AND probe_type = ?";
        Object[] args = {probeId, probeType.name()};

        jdbcTemplate.update(sql, args);
    }

    @Override
    public void delete(List<String> probeIds, ProbeType probeType) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE probe_id IN (?) AND probe_type = ?";
        Object[] args = {probeIds.toArray(), probeType.name()};

        jdbcTemplate.update(sql, args);
    }
}
