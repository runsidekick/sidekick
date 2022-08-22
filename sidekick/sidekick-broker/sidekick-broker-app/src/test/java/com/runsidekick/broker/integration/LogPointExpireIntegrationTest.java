package com.runsidekick.broker.integration;

import com.runsidekick.broker.integration.setup.BrokerBaseIntegrationTest;
import com.runsidekick.broker.integration.setup.WebSocketClient;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.event.impl.LogPointEvent;
import com.runsidekick.broker.model.request.impl.logpoint.ListLogPointsRequest;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.UpdateLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.ListLogPointsResponse;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.UpdateLogPointResponse;
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
 * @author yasin.kalafat
 */
@TestPropertySource(properties = {"broker.port: 4242"})
public class LogPointExpireIntegrationTest extends BrokerBaseIntegrationTest {

    @Autowired
    private RedissonClient client;

    @Test
    public void clientShouldBeAbleAddExpireCount_whenPuttingLogPoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutLogPointRequest putLogPointRequest = getPutLogPointRequest(requestId);
            webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);
            String logPointId = getLogPointId(putLogPointRequest.getFileName(),
                    putLogPointRequest.getLineNo(), putLogPointRequest.getClient());
            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, logPointId));
            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.get()).isEqualTo(putLogPointRequest.getExpireCount());
                long remainTimeToLive = value.remainTimeToLive();
                assertThat(remainTimeToLive)
                        .isLessThan(TimeUnit.SECONDS.toMillis(putLogPointRequest.getExpireSecs()));
                assertThat(remainTimeToLive).isPositive();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleUpdateExpireCount_whenUpdatingLogPoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutLogPointRequest putLogPointRequest = getPutLogPointRequest(requestId);
            webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);
            String logPointId = getLogPointId(putLogPointRequest.getFileName(),
                    putLogPointRequest.getLineNo(), putLogPointRequest.getClient());
            requestId = UUID.randomUUID().toString();
            UpdateLogPointRequest updateLogPointRequest = new UpdateLogPointRequest();
            updateLogPointRequest.setId(requestId);
            updateLogPointRequest.setConditionExpression("test == 2");
            updateLogPointRequest.setExpireCount(5);
            updateLogPointRequest.setExpireSecs(15);
            updateLogPointRequest.setLogPointId(logPointId);
            updateLogPointRequest.setLogExpression("test");
            updateLogPointRequest.setStdoutEnabled(true);
            updateLogPointRequest.setLogLevel("INFO");
            updateLogPointRequest.setPersist(true);

            webSocketUserClient.request(updateLogPointRequest, UpdateLogPointResponse.class);

            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, logPointId));
            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.get()).isEqualTo(updateLogPointRequest.getExpireCount());
                long remainTimeToLive = value.remainTimeToLive();
                assertThat(remainTimeToLive)
                        .isLessThan(TimeUnit.SECONDS.toMillis(updateLogPointRequest.getExpireSecs()));
                assertThat(remainTimeToLive).isPositive();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleRemoveExpireCount_whenRemovingLogPoint() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String requestId = UUID.randomUUID().toString();
            PutLogPointRequest putLogPointRequest = getPutLogPointRequest(requestId);
            webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

            String logPointId = getLogPointId(putLogPointRequest.getFileName(),
                    putLogPointRequest.getLineNo(), putLogPointRequest.getClient());

            requestId = UUID.randomUUID().toString();
            RemoveLogPointRequest removeLogPointRequest = new RemoveLogPointRequest();
            removeLogPointRequest.setId(requestId);
            removeLogPointRequest.setLogPointId(logPointId);
            removeLogPointRequest.setPersist(true);

            webSocketUserClient.request(removeLogPointRequest, RemoveLogPointResponse.class);

            String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, logPointId));

            assertEventually(() -> {
                RAtomicLong value = client.getAtomicLong(expireCountId);
                assertThat(value.isExists()).isFalse();
            });
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void brokerShouldBeAbleRemoveLogPoint_whenWhenExpireCountLimitExceeded() {
        WebSocketClient webSocketUserClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);
            String[] appClientProps = {"123", "app1a", "dev", "1.0.1-SNAPSHOT"};
            WebSocketClient webSocketAppClient = getWebSocketAppClient(appClientProps);
            try {
                assertConnected(webSocketAppClient);
                String requestId = UUID.randomUUID().toString();
                PutLogPointRequest putLogPointRequest = getPutLogPointRequest(requestId);
                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);
                String logPointId = getLogPointId(putLogPointRequest.getFileName(),
                        putLogPointRequest.getLineNo(), putLogPointRequest.getClient());
                String expireCountId = getExpireCountId(buildResourceKey(WORKSPACE_ID, logPointId));

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.get()).isEqualTo(putLogPointRequest.getExpireCount());
                });

                LogPointEvent event = new LogPointEvent(logPointId,
                        putLogPointRequest.getFileName(), "Test",
                        putLogPointRequest.getLineNo(), "testMethod", UUID.randomUUID().toString());
                event.setId(UUID.randomUUID().toString());
                event.setClient(CLIENT);
                webSocketAppClient.send(event);

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                assertEventually(() -> {
                    RAtomicLong value = client.getAtomicLong(expireCountId);
                    assertThat(value.isExists()).isFalse();
                    ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(listLogPointRequest,
                            ListLogPointsResponse.class);
                    assertThat(listLogPointResponse.getLogPoints()).isEmpty();
                });

            } finally {
                webSocketAppClient.close();
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    private PutLogPointRequest getPutLogPointRequest(String requestId) {
        List<ApplicationFilter> filters = new ArrayList<>();
        ApplicationFilter filter = new ApplicationFilter();
        filter.setName("app1a");
        filters.add(filter);

        PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
        putLogPointRequest.setId(requestId);
        putLogPointRequest.setFileName("Test.java");
        putLogPointRequest.setClient(CLIENT);
        putLogPointRequest.setLineNo(10);
        putLogPointRequest.setPersist(true);
        putLogPointRequest.setConditionExpression("test == 1");
        putLogPointRequest.setApplicationFilters(filters);
        putLogPointRequest.setExpireCount(1);
        putLogPointRequest.setExpireSecs(20);
        putLogPointRequest.setLogExpression("test");
        putLogPointRequest.setStdoutEnabled(true);
        putLogPointRequest.setLogLevel("INFO");
        return putLogPointRequest;
    }

    private WebSocketClient getWebSocketAppClient(String... args) {
        return new WebSocketClient(port, createAppCredentials(API_KEY, args[0], args[1], args[2], args[3]));
    }

    private static String buildResourceKey(String workspaceId, String logPointId) {
        return workspaceId + "_" + logPointId;
    }

    private static String getExpireCountId(String logPointId) {
        return "LogPoint::ExpireCount:" + logPointId;
    }

    private static String getLogPointId(String fileName, int lineNo, String client) {
        return fileName + "::" + lineNo + "::" + client;
    }
}
