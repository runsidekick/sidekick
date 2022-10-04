package com.runsidekick.broker.integration;

import com.runsidekick.broker.integration.setup.WebSocketClient;
import com.runsidekick.broker.integration.setup.BrokerBaseIntegrationTest;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.event.impl.TracePointSnapshotEvent;
import com.runsidekick.broker.model.request.impl.tracepoint.ListTracePointsRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.ListTracePointsResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;
import org.junit.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tolgatakir
 */
@TestPropertySource(properties = {"broker.port: 4242"})
public class TracePointExpireIntegrationTest extends BrokerBaseIntegrationTest {

    @Autowired
    private RedissonClient client;

    @Test
    public void clientShouldBeAbleAddExpireCount_whenPuttingTracePoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutTracePointRequest putTracePointRequest = getPutTracePointRequest(requestId);
            webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);
            String tracePointId = getTracePointId(putTracePointRequest.getFileName(),
                    putTracePointRequest.getLineNo(), putTracePointRequest.getClient());
            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, tracePointId));
            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.get()).isEqualTo(putTracePointRequest.getExpireCount());
                long remainTimeToLive = value.remainTimeToLive();
                assertThat(remainTimeToLive)
                        .isLessThan(TimeUnit.SECONDS.toMillis(putTracePointRequest.getExpireSecs()));
                assertThat(remainTimeToLive).isPositive();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleUpdateExpireCount_whenUpdatingTracePoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutTracePointRequest putTracePointRequest = getPutTracePointRequest(requestId);
            webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);
            String tracePointId = getTracePointId(putTracePointRequest.getFileName(),
                    putTracePointRequest.getLineNo(), putTracePointRequest.getClient());
            requestId = UUID.randomUUID().toString();
            UpdateTracePointRequest updateTracePointRequest = new UpdateTracePointRequest();
            updateTracePointRequest.setId(requestId);
            updateTracePointRequest.setConditionExpression("test == 2");
            updateTracePointRequest.setExpireCount(5);
            updateTracePointRequest.setExpireSecs(15);
            updateTracePointRequest.setEnableTracing(false);
            updateTracePointRequest.setTracePointId(tracePointId);
            updateTracePointRequest.setPersist(true);

            webSocketUserClient.request(updateTracePointRequest, UpdateTracePointResponse.class);

            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, tracePointId));
            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.get()).isEqualTo(updateTracePointRequest.getExpireCount());
                long remainTimeToLive = value.remainTimeToLive();
                assertThat(remainTimeToLive)
                        .isLessThan(TimeUnit.SECONDS.toMillis(updateTracePointRequest.getExpireSecs()));
                assertThat(remainTimeToLive).isPositive();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleRemoveExpireCount_whenRemovingTracePoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutTracePointRequest putTracePointRequest = getPutTracePointRequest(requestId);
            webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

            String tracePointId = getTracePointId(putTracePointRequest.getFileName(),
                    putTracePointRequest.getLineNo(), putTracePointRequest.getClient());

            requestId = UUID.randomUUID().toString();
            RemoveTracePointRequest removeTracePointRequest = new RemoveTracePointRequest();
            removeTracePointRequest.setId(requestId);
            removeTracePointRequest.setTracePointId(tracePointId);
            removeTracePointRequest.setPersist(true);

            webSocketUserClient.request(removeTracePointRequest, RemoveTracePointResponse.class);

            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, tracePointId));

            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.isExists()).isFalse();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void brokerShouldBeAbleRemoveTracePoint_whenWhenExpireCountLimitExceeded() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String[] appClientProps = {"123", "app1a", "dev", "1.0.1-SNAPSHOT"};
            WebSocketClient webSocketAppClient = getWebSocketAppClient(appClientProps);
            try {
                assertConnected(webSocketAppClient);
                String requestId = UUID.randomUUID().toString();
                PutTracePointRequest putTracePointRequest = getPutTracePointRequest(requestId);
                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);
                String tracePointId = getTracePointId(putTracePointRequest.getFileName(),
                        putTracePointRequest.getLineNo(), putTracePointRequest.getClient());
                String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, tracePointId));

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.get()).isEqualTo(putTracePointRequest.getExpireCount());
                });

                TracePointSnapshotEvent event = new TracePointSnapshotEvent(tracePointId,
                        putTracePointRequest.getFileName(), "Test",
                        putTracePointRequest.getLineNo(), "testMethod", UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(), UUID.randomUUID().toString());
                event.setId(UUID.randomUUID().toString());
                event.setClient(CLIENT);
                webSocketAppClient.send(event);

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.isExists()).isFalse();
                    ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(listTracePointRequest,
                            ListTracePointsResponse.class);
                    assertThat(listTracePointResponse.getTracePoints()).isEmpty();
                });

            } finally {
                webSocketAppClient.close();
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void brokerShouldNotBeAbleRemovePredefinedTracePoint_whenExpireCountLimitExceeded() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String[] appClientProps = {"123", "app1a", "dev", "1.0.1-SNAPSHOT"};
            WebSocketClient webSocketAppClient = getWebSocketAppClient(appClientProps);
            try {
                assertConnected(webSocketAppClient);
                String requestId = UUID.randomUUID().toString();
                PutTracePointRequest putTracePointRequest = getPutTracePointRequest(requestId);
                putTracePointRequest.setPredefined(true);
                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);
                String tracePointId = getTracePointId(putTracePointRequest.getFileName(),
                        putTracePointRequest.getLineNo(), putTracePointRequest.getClient());
                String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, tracePointId));

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.get()).isEqualTo(0);
                });

                TracePointSnapshotEvent event = new TracePointSnapshotEvent(tracePointId,
                        putTracePointRequest.getFileName(), "Test",
                        putTracePointRequest.getLineNo(), "testMethod", UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(), UUID.randomUUID().toString());
                event.setId(UUID.randomUUID().toString());
                event.setClient(CLIENT);
                webSocketAppClient.send(event);

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.isExists()).isFalse();
                    ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(listTracePointRequest,
                            ListTracePointsResponse.class);
                    assertThat(listTracePointResponse.getTracePoints().size()).isEqualTo(1);
                });

            } finally {
                webSocketAppClient.close();
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    private PutTracePointRequest getPutTracePointRequest(String requestId) {
        List<ApplicationFilter> filters = new ArrayList<>();
        ApplicationFilter filter = new ApplicationFilter();
        filter.setName("app1a");
        filters.add(filter);

        PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
        putTracePointRequest.setId(requestId);
        putTracePointRequest.setFileName("Test.java");
        putTracePointRequest.setClient(CLIENT);
        putTracePointRequest.setLineNo(10);
        putTracePointRequest.setPersist(true);
        putTracePointRequest.setConditionExpression("test == 1");
        putTracePointRequest.setApplicationFilters(filters);
        putTracePointRequest.setExpireCount(1);
        putTracePointRequest.setExpireSecs(20);
        putTracePointRequest.setEnableTracing(true);
        return putTracePointRequest;
    }

    private WebSocketClient getWebSocketAppClient(String... args) {
        return new WebSocketClient(port, createAppCredentials(API_KEY, args[0], args[1], args[2], args[3]));
    }

    private static String buildResourceKey(String workspaceId, String tracePointId) {
        return workspaceId + "_" + tracePointId;
    }

    private static String getExpireCountId(String tracePointId) {
        return "TracePoint::ExpireCount:" + tracePointId;
    }

    private static String getTracePointId(String fileName, int lineNo, String client) {
        return fileName + "::" + lineNo + "::" + client;
    }
}
