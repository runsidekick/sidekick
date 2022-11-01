package com.runsidekick.api.integration.setup;

import io.thundra.swark.env.utils.MySQLContainerContext;
import io.thundra.swark.env.utils.RedisContainerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.sql.SQLException;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration(initializers = ContainerizedContextInitializer.class)

public abstract class BaseIntegrationTest {

    protected final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ContainerContextHolder containerContextHolder;

    @LocalServerPort
    protected int port;


    @BeforeEach
    public void setup() throws Exception {
        preSetup();
        doSetup();
        postSetup();
    }

    protected void preSetup() {
    }

    protected void doSetup() throws Exception {
    }

    protected void postSetup() {
    }

    @AfterEach
    public void tearDown() {
        try {
            preTearDown();
            doTearDown();
            postTearDown();
        } finally {
            clearDataSources();
        }
    }

    protected void preTearDown() {
    }

    protected void doTearDown() {
    }

    protected void postTearDown() {
    }

    private void clearDataSources() {
        try {
            clearDB();
        } catch (Throwable t) {
            logger.error("Unable to clear DB", t);
        }
        try {
            clearRedis();
        } catch (Throwable t) {
            logger.error("Unable to clear Redis", t);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    protected <R> ResponseEntity<R> doRequest(HttpMethod httpMethod, String path,
                                              String body, Class<R> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return doRequest(httpMethod, headers, path, body, responseType);
    }

    protected <R> ResponseEntity<R> doRequest(HttpMethod httpMethod, HttpHeaders headers,

                                              String path, String body, Class<R> responseType) {
        HttpEntity requestEntity = new HttpEntity(body, headers);
        return restTemplate.exchange(createURLWithPort(path), httpMethod, requestEntity, responseType);
    }

    protected String createURLWithPort(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "http://localhost:" + port + path;
    }

    protected <R> ResponseEntity<R> get(String path, Class<R> responseType) {
        return doRequest(HttpMethod.GET, path, null, responseType);
    }

    protected <R> ResponseEntity<R> post(String path, String body, Class<R> responseType) {
        return doRequest(HttpMethod.POST, path, body, responseType);
    }

    protected <R> ResponseEntity<R> put(String path, String body, Class<R> responseType) {
        return doRequest(HttpMethod.PUT, path, body, responseType);
    }

    protected <R> ResponseEntity<R> patch(String path, String body, Class<R> responseType) {
        return doRequest(HttpMethod.PATCH, path, body, responseType);
    }

    protected <R> ResponseEntity<R> delete(String path, String body, Class<R> responseType) {
        return doRequest(HttpMethod.DELETE, path, body, responseType);
    }

    private void clearDB() throws SQLException, IOException {
        MySQLContainerContext mySQLContainerContext = containerContextHolder.mySQLContainerContext;
        if (mySQLContainerContext != null) {
            mySQLContainerContext.clearDB();
        }
    }

    private void clearRedis() {
        RedisContainerContext redisContainerContext = containerContextHolder.redisContainerContext;
        if (redisContainerContext != null) {
            redisContainerContext.clearCache();
        }
    }
}
