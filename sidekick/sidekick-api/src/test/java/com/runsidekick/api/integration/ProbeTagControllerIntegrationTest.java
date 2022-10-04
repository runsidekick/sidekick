package com.runsidekick.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.api.integration.setup.BaseIntegrationTest;
import com.runsidekick.api.service.ApiAuthService;
import com.runsidekick.broker.util.Constants;
import com.runsidekick.model.ProbeTag;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.net.URISyntaxException;
import java.util.List;

import static com.runsidekick.api.utils.TestUtils.getMockAuthenticatedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author yasin
 */
public class ProbeTagControllerIntegrationTest extends BaseIntegrationTest {

    private static final String API_BASE_PATH = "/api/v1/probetags";
    private static final String[] MODEL_ATTRIBUTES = new String[]{
            "id", "tag"};

    private static final String TABLE_NAME = "ProbeTag";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProbeTag mockProbeTag = ProbeTag.builder()
            .id("test-id")
            .tag("test-tag")
            .workspaceId(Constants.WORKSPACE_ID)
            .build();

    private final RowMapper<ProbeTag> rowMapper = new BeanPropertyRowMapper<>(ProbeTag.class);

    @MockBean
    private ApiAuthService apiAuthService;

    @Override
    protected void doSetup() {
        when(apiAuthService.getCurrentUser()).thenReturn(getMockAuthenticatedUser());
    }

    @Test
    void testListProbeTags() throws URISyntaxException {
        insertMockProbeTag();
        assertThat(getCountInDb()).isEqualTo(1);

        URIBuilder uriBuilder = new URIBuilder(API_BASE_PATH);
        ResponseEntity<List> response = get(uriBuilder.build().toString(), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().get(0))
                .extracting(MODEL_ATTRIBUTES)
                .containsExactly(mockProbeTag.getId(), mockProbeTag.getTag());
    }

    @Test
    void testAddProbeTag() throws URISyntaxException, JsonProcessingException {
        URIBuilder uriBuilder = new URIBuilder(API_BASE_PATH);

        String jsonBody = objectMapper.writeValueAsString(mockProbeTag);

        ResponseEntity<ProbeTag> response = post(uriBuilder.build().toString(), jsonBody, ProbeTag.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MODEL_ATTRIBUTES)
                .contains(mockProbeTag.getTag());
        assertThat(getCountInDb()).isEqualTo(1);
    }

    @Test
    void testDeleteProbeTag() throws URISyntaxException {
        insertMockProbeTag();
        assertThat(getCountInDb()).isEqualTo(1);

        URIBuilder uriBuilder = new URIBuilder(API_BASE_PATH + "/" + mockProbeTag.getId());
        ResponseEntity response = delete(uriBuilder.build().toString(), "{}", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getCountInDb()).isZero();
    }

    private void insertMockProbeTag() {
        jdbcTemplate.update("INSERT INTO " + TABLE_NAME + " (id, workspace_id, tag) " +
                        "VALUES (?, ?, ?)",
                mockProbeTag.getId(),
                mockProbeTag.getWorkspaceId(),
                mockProbeTag.getTag());
    }

    private Integer getCountInDb() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + TABLE_NAME, Integer.class);
    }

    private ProbeTag getProbeTagFromDb(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id=?";
        Object[] args = {id};
        return jdbcTemplate.queryForObject(sql, rowMapper, args);
    }

}
