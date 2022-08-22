package com.runsidekick.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author Suleyman Barman
 * @version 9.04.2020
 */
@Repository
public class BaseDBRepository {

    protected static final long FIVE_MINUTE = 300000;
    protected static final long ONE_DAY = 24 * 60 * 60 * 1000;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    protected ObjectMapper mapper;

}
