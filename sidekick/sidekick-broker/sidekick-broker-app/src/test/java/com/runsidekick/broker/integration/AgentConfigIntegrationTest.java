package com.runsidekick.broker.integration;

import com.runsidekick.broker.integration.setup.BrokerBaseIntegrationTest;
import com.runsidekick.broker.integration.setup.WebSocketClient;
import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.config.AttachRequest;
import com.runsidekick.broker.model.request.impl.config.DetachRequest;
import com.runsidekick.broker.model.request.impl.config.GetConfigRequest;
import com.runsidekick.broker.model.request.impl.config.UpdateConfigRequest;
import com.runsidekick.broker.model.response.impl.config.AttachResponse;
import com.runsidekick.broker.model.response.impl.config.DetachResponse;
import com.runsidekick.broker.model.response.impl.config.GetConfigResponse;
import com.runsidekick.broker.model.response.impl.config.UpdateConfigResponse;
import com.runsidekick.broker.service.ApplicationConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author yasin.kalafat
 */
@TestPropertySource(properties = {"broker.port: 4242"})
public class AgentConfigIntegrationTest extends BrokerBaseIntegrationTest {

    @Autowired
    private ApplicationConfigService applicationConfigService;

    @Test
    public void clientShouldBeAbleDetachAgents() throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketClient webSocketUserClient = getWebSocketUserClient();
        List<ApplicationFilter> applicationFilters = getApplicationFilters();
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient2a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient2a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app2a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue2");
                                            put("tag2", "tagValue4");
                                        }}));
                assertConnected(webSocketAppClient2a);

                String requestId = UUID.randomUUID().toString();
                DetachRequest detachRequest = new DetachRequest();
                detachRequest.setId(requestId);
                detachRequest.setApplicationFilters(applicationFilters);

                CompletableFuture completableFuture = registerForWaitingClientMessage(requestId);

                webSocketAppClient1a.clearReadMessages();
                webSocketAppClient2a.clearReadMessages();

                webSocketUserClient.requestSync(detachRequest, DetachResponse.class);

                completableFuture.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Detach request should be received by app1
                DetachRequest receivedDetachRequest =
                        webSocketAppClient1a.read(DetachRequest.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedDetachRequest);
                assertEquals(requestId, receivedDetachRequest.getId());

                // Detach request should not be received by app2
                DetachRequest receivedDetachRequest2 =
                        webSocketAppClient2a.read(DetachRequest.class, 3, TimeUnit.SECONDS);
                assertNull(receivedDetachRequest2);

                DetachResponse detachResponse = new DetachResponse();
                detachResponse.setRequestId(requestId);
                detachResponse.setErroneous(false);
                detachResponse.setClient(CLIENT);

                webSocketUserClient.clearReadMessages();

                webSocketAppClient1a.send(detachResponse);

                // Detach response be received by client
                DetachResponse receivedDetachResponse =
                        webSocketUserClient.read(DetachResponse.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedDetachResponse);
                assertEquals(requestId, receivedDetachResponse.getRequestId());

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ApplicationConfig applicationConfig =
                        applicationConfigService.getApplicationConfig(WORKSPACE_ID, applicationFilters.get(0));

                assertTrue(applicationConfig.isDetached());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient2a != null) {
                    webSocketAppClient2a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleAttachAgents() throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketClient webSocketUserClient = getWebSocketUserClient();
        List<ApplicationFilter> applicationFilters = getApplicationFilters();
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient2a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient2a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app2a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue2");
                                            put("tag2", "tagValue4");
                                        }}));
                assertConnected(webSocketAppClient2a);

                String requestId = UUID.randomUUID().toString();
                AttachRequest attachRequest = new AttachRequest();
                attachRequest.setId(requestId);
                attachRequest.setApplicationFilters(applicationFilters);

                CompletableFuture completableFuture = registerForWaitingClientMessage(requestId);

                webSocketAppClient1a.clearReadMessages();
                webSocketAppClient2a.clearReadMessages();

                webSocketUserClient.requestSync(attachRequest, AttachResponse.class);

                completableFuture.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Attach request should be received by app1
                AttachRequest receivedAttachRequest =
                        webSocketAppClient1a.read(AttachRequest.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedAttachRequest);
                assertEquals(requestId, receivedAttachRequest.getId());

                // Attach request should not be received by app2
                AttachRequest receivedAttachRequest2 =
                        webSocketAppClient2a.read(AttachRequest.class, 3, TimeUnit.SECONDS);
                assertNull(receivedAttachRequest2);

                AttachResponse attachResponse = new AttachResponse();
                attachResponse.setRequestId(requestId);
                attachResponse.setErroneous(false);
                attachResponse.setClient(CLIENT);

                webSocketUserClient.clearReadMessages();

                webSocketAppClient1a.send(attachResponse);

                // Attach response be received by client
                AttachResponse receivedAttachResponse =
                        webSocketUserClient.read(AttachResponse.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedAttachResponse);
                assertEquals(requestId, receivedAttachResponse.getRequestId());

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ApplicationConfig applicationConfig =
                        applicationConfigService.getApplicationConfig(WORKSPACE_ID, applicationFilters.get(0));

                assertFalse(applicationConfig.isDetached());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient2a != null) {
                    webSocketAppClient2a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeUpdateConfig() throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketClient webSocketUserClient = getWebSocketUserClient();
        List<ApplicationFilter> applicationFilters = getApplicationFilters();
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient2a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient2a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app2a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue2");
                                            put("tag2", "tagValue4");
                                        }}));
                assertConnected(webSocketAppClient2a);

                String requestId = UUID.randomUUID().toString();
                UpdateConfigRequest updateConfigRequest = new UpdateConfigRequest();
                updateConfigRequest.setId(requestId);
                updateConfigRequest.setApplicationFilters(applicationFilters);
                updateConfigRequest.setConfig(getConfig());

                CompletableFuture completableFuture = registerForWaitingClientMessage(requestId);

                webSocketAppClient1a.clearReadMessages();
                webSocketAppClient2a.clearReadMessages();

                webSocketUserClient.requestSync(updateConfigRequest, UpdateConfigResponse.class);

                completableFuture.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Update Config request should be received by app1
                UpdateConfigRequest receivedUpdateConfigRequest =
                        webSocketAppClient1a.read(UpdateConfigRequest.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedUpdateConfigRequest);
                assertEquals(requestId, receivedUpdateConfigRequest.getId());

                // Update Config request should not be received by app2
                UpdateConfigRequest receivedUpdateConfigRequest2 =
                        webSocketAppClient2a.read(UpdateConfigRequest.class, 3, TimeUnit.SECONDS);
                assertNull(receivedUpdateConfigRequest2);

                UpdateConfigResponse updateConfigResponse = new UpdateConfigResponse();
                updateConfigResponse.setRequestId(requestId);
                updateConfigResponse.setErroneous(false);
                updateConfigResponse.setClient(CLIENT);

                webSocketUserClient.clearReadMessages();

                webSocketAppClient1a.send(updateConfigResponse);

                // Update Config response be received by client
                UpdateConfigResponse receivedUpdateConfigResponse =
                        webSocketUserClient.read(UpdateConfigResponse.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedUpdateConfigResponse);
                assertEquals(requestId, receivedUpdateConfigResponse.getRequestId());

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ApplicationConfig applicationConfig =
                        applicationConfigService.getApplicationConfig(WORKSPACE_ID, applicationFilters.get(0));

                assertThat(applicationConfig.getConfig()).isEqualTo(getConfig());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient2a != null) {
                    webSocketAppClient2a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void appShouldBeGetConfig() {
        String[] appClientProps = {"123", "app1a", "dev", "1.0.1-SNAPSHOT"};
        WebSocketClient webSocketAppClient = getWebSocketAppClient(appClientProps);
        try {
            applicationConfigService.saveApplicationConfig(ApplicationConfig.builder()
                    .workspaceId(WORKSPACE_ID)
                    .applicationFilter(getApplicationFilters().get(0))
                    .config(getConfig())
                    .build());

            assertConnected(webSocketAppClient);

            String requestId = UUID.randomUUID().toString();
            GetConfigRequest getConfigRequest = new GetConfigRequest();
            getConfigRequest.setId(requestId);

            GetConfigResponse getConfigResponse =
                    webSocketAppClient.requestSync(getConfigRequest, GetConfigResponse.class);

            assertThat(getConfigResponse.getConfig()).isEqualTo(getConfig());

        } finally {
            webSocketAppClient.close();
        }
    }

    private List<ApplicationFilter> getApplicationFilters() {
        List<ApplicationFilter> filters = new ArrayList<>();
        ApplicationFilter filter = new ApplicationFilter();
        filter.setName("app1a");
        filter.setStage("dev");
        filter.setVersion("1.0.1-SNAPSHOT");
        filters.add(filter);
        return filters;
    }

    private Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("prop_1", "test");
        config.put("prop_2", "test2");
        return config;
    }

    private WebSocketClient getWebSocketUserClient() {
        return new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
    }

    private WebSocketClient getWebSocketAppClient(String... args) {
        return new WebSocketClient(port, createAppCredentials(API_KEY, args[0], args[1], args[2], args[3]));
    }

}
