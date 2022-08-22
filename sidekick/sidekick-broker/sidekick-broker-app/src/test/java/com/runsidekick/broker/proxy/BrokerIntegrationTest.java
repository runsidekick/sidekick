package com.runsidekick.broker.proxy;

import com.runsidekick.broker.error.ErrorCodes;
import com.runsidekick.broker.integration.setup.BrokerBaseIntegrationTest;
import com.runsidekick.broker.integration.setup.WebSocketClient;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.event.impl.ApplicationDisconnectEvent;
import com.runsidekick.broker.model.event.impl.ApplicationStatusEvent;
import com.runsidekick.broker.model.request.impl.BaseRequest;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.model.request.impl.ListWebhooksRequest;
import com.runsidekick.broker.model.request.impl.logpoint.DisableLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.EnableLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.ListLogPointsRequest;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveLogPointRequest;
import com.runsidekick.broker.model.request.impl.logpoint.UpdateLogPointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.DisableTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.EnableTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.ListTracePointsRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveTracePointRequest;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.BaseApplicationResponse;
import com.runsidekick.broker.model.response.impl.ListApplicationsResponse;
import com.runsidekick.broker.model.response.impl.ListWebhooksResponse;
import com.runsidekick.broker.model.response.impl.logpoint.DisableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.EnableLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.ListLogPointsResponse;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
import com.runsidekick.broker.model.response.impl.logpoint.UpdateLogPointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.DisableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.EnableTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.ListTracePointsResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.TracePointService;
import com.runsidekick.model.WebhookType;
import com.runsidekick.model.dto.WebhookDto;
import lombok.SneakyThrows;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author serkan.ozal
 */
public class BrokerIntegrationTest extends BrokerBaseIntegrationTest {

    @Autowired
    protected TracePointService tracePointService;

    @Autowired
    protected LogPointService logPointService;

    protected void whenVerifyToken(String token) throws Exception {
    }

    protected void whenVerifyAnyToken(String token) throws Exception {
    }

    @Test
    public void appConnectionShouldBeClosed_when_authFailed_becauseOf_missingCredentials() {
        WebSocketClient webSocketClient = new WebSocketClient(port, createEmptyAppCredentials());
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    @Test
    public void appConnectionShouldBeClosed_when_authFailed_becauseOf_missingApiKey() {
        WebSocketClient webSocketClient =
                new WebSocketClient(port, createAppCredentials(null, "123"));
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    @Test
    public void appConnectionShouldBeClosed_when_authFailed_becauseOf_missingAppInstanceId() {
        WebSocketClient webSocketClient =
                new WebSocketClient(port, createAppCredentials(API_KEY, null));
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    @Test
    public void clientConnectionShouldBeClosed_when_authFailed_becauseOf_missingCredentials() {
        WebSocketClient webSocketClient = new WebSocketClient(port, createEmptyClientCredentials());
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void appConnectionShouldBeClosed_when_authFailed_becauseOf_invalidToken() {
        WebSocketClient webSocketClient =
                new WebSocketClient(port, createAppCredentials("xyz", "123"));
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void clientConnectionShouldBeClosed_when_authFailed_becauseOf_invalidToken() {
        WebSocketClient webSocketClient =
                new WebSocketClient(port, createClientTokenCredentials("test1"));
        try {
            assertClosed(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    @Test
    public void appConnectionShouldBeAlive_when_authSucceeded() {
        WebSocketClient webSocketClient =
                new WebSocketClient(port, createAppCredentials(API_KEY, "xyz"));
        try {
            assertConnected(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void clientConnectionShouldBeAlive_when_tokenAuthSucceeded() throws Exception {
        whenVerifyToken(USER_TOKEN);

        WebSocketClient webSocketClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketClient);
        } finally {
            webSocketClient.close();
        }
    }

    @Test
    public void existingAppConnectionShouldBeClosed_when_sessionAlreadyExist() {
        WebSocketClient existingWebSocketAppClient =
                new WebSocketClient(port, createAppCredentials(API_KEY, "123"));
        try {
            assertConnected(existingWebSocketAppClient);

            WebSocketClient newWebSocketAppClient =
                    new WebSocketClient(port, createAppCredentials(API_KEY, "123"));
            try {
                assertClosed(existingWebSocketAppClient);
            } finally {
                newWebSocketAppClient.close();
            }
        } finally {
            existingWebSocketAppClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void existingClientConnectionShouldBeConnected_when_sessionAlreadyExist() {
        WebSocketClient existingWebSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(existingWebSocketUserClient);

            WebSocketClient newWebSocketUserClient =
                    new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
            try {
                assertConnected(existingWebSocketUserClient);
            } finally {
                newWebSocketUserClient.close();
            }
        } finally {
            existingWebSocketUserClient.close();
        }
    }

    @SneakyThrows
    @Test
    public void existingClientConnectionShouldBeConnected_when_sessionAlreadyExist_withToken() {
        whenVerifyToken(USER_TOKEN);

        WebSocketClient existingWebSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));

        try {
            assertConnected(existingWebSocketUserClient);

            WebSocketClient newWebSocketUserClient =
                    new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
            try {
                assertConnected(existingWebSocketUserClient);
            } finally {
                newWebSocketUserClient.close();
            }
        } finally {
            existingWebSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToSendMessageToSingleApp() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1 = null;
            WebSocketClient webSocketAppClient2 = null;

            try {
                webSocketAppClient1 =
                        new WebSocketClient(port, createAppCredentials(API_KEY, "123"));
                assertConnected(webSocketAppClient1);

                webSocketAppClient2 =
                        new WebSocketClient(port, createAppCredentials(API_KEY, "456"));
                assertConnected(webSocketAppClient2);

                TestRequest message = new TestRequest(Arrays.asList("123"));

                webSocketUserClient.send(message);

                TestRequest app1Message = webSocketAppClient1.read(TestRequest.class, WAIT_SECS, TimeUnit.SECONDS);
                TestRequest app2Message = webSocketAppClient2.read(TestRequest.class, WAIT_SECS, TimeUnit.SECONDS);

                assertThat(app1Message, is(message));
                assertThat(app2Message, is(IsNull.nullValue()));
            } finally {
                if (webSocketAppClient1 != null) {
                    webSocketAppClient1.close();
                }
                if (webSocketAppClient2 != null) {
                    webSocketAppClient2.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void appShouldBeAbleToSendMessageToClient() {
        WebSocketClient webSocketAppClient =
                new WebSocketClient(port, createAppCredentials(API_KEY, "123"));
        try {
            assertConnected(webSocketAppClient);

            WebSocketClient webSocketUserClient1 = null;

            try {
                webSocketUserClient1 =
                        new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
                assertConnected(webSocketUserClient1);

                TestApplicationRequest message = new TestApplicationRequest(CLIENT);

                webSocketAppClient.send(message);

                TestApplicationRequest client1Message = webSocketUserClient1.read(TestApplicationRequest.class, WAIT_SECS, TimeUnit.SECONDS);

                assertThat(client1Message, is(message));
            } finally {
                if (webSocketUserClient1 != null) {
                    webSocketUserClient1.close();
                }
            }
        } finally {
            webSocketAppClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void clientShouldBeAbleToSendMessageToMultipleApps() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1 = null;
            WebSocketClient webSocketAppClient2 = null;

            try {
                webSocketAppClient1 =
                        new WebSocketClient(port, createAppCredentials(API_KEY, "123"));
                assertConnected(webSocketAppClient1);

                webSocketAppClient2 =
                        new WebSocketClient(port, createAppCredentials(API_KEY, "456"));
                assertConnected(webSocketAppClient2);

                TestRequest message = new TestRequest(Arrays.asList("123", "456"));

                webSocketUserClient.send(message);

                TestRequest app1Message = webSocketAppClient1.read(TestRequest.class, WAIT_SECS, TimeUnit.SECONDS);
                TestRequest app2Message = webSocketAppClient2.read(TestRequest.class, WAIT_SECS, TimeUnit.SECONDS);

                assertThat(app1Message, is(message));
                assertThat(app2Message, is(message));
            } finally {
                if (webSocketAppClient1 != null) {
                    webSocketAppClient1.close();
                }
                if (webSocketAppClient2 != null) {
                    webSocketAppClient2.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }


    @Test
    public void clientShouldBeAbleToBeNotifiedWhenNewAppJoins() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient);

                ApplicationStatusEvent applicationStatusEvent =
                        webSocketUserClient.read(ApplicationStatusEvent.class, WAIT_SECS, TimeUnit.SECONDS);

                assertThat(applicationStatusEvent, is(IsNull.notNullValue()));
                Application application = applicationStatusEvent.getApplication();
                assertApplication(application, "123", "app1a", "dev", "1.0.1-SNAPSHOT");
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToBeNotifiedWhenAppDisconnects() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient);
                webSocketUserClient.read(ApplicationStatusEvent.class, WAIT_SECS, TimeUnit.SECONDS);

                webSocketAppClient.close();
                assertClosed(webSocketAppClient);

                ApplicationDisconnectEvent applicationDisconnectEvent =
                        webSocketUserClient.read(ApplicationDisconnectEvent.class, WAIT_SECS, TimeUnit.SECONDS);

                assertThat(applicationDisconnectEvent, is(IsNull.notNullValue()));
                String applicationInstanceId = applicationDisconnectEvent.getApplicationInstanceId();
                assertEquals(applicationInstanceId, "123");
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    private void assertApplication(Application app, String appInstanceId,
                                   String appName, String appStage, String appVersion) {
        assertThat(app.getInstanceId(), is(appInstanceId));
        assertThat(app.getName(), is(appName));
        assertThat(app.getStage(), is(appStage));
        assertThat(app.getVersion(), is(appVersion));
    }

    private void assertTracePoint(TracePoint tracePoint,
                                  String fileName, int lineNo, String client) {
        assertThat(tracePoint.getFileName(), is(fileName));
        assertThat(tracePoint.getLineNo(), is(lineNo));
        assertThat(tracePoint.getClient(), is(client));
    }

    private void assertLogPoint(LogPoint logPoint,
                                String fileName, int lineNo, String client) {
        assertThat(logPoint.getFileName(), is(fileName));
        assertThat(logPoint.getLineNo(), is(lineNo));
        assertThat(logPoint.getClient(), is(client));
    }

    private void assertWebhook(WebhookDto webhook, String id, String name, String workspaceId, WebhookType type) {
        assertThat(webhook.getId(), is(id));
        assertThat(webhook.getName(), is(name));
        assertThat(webhook.getWorkspaceId(), is(workspaceId));
        assertThat(webhook.getType(), is(type));
    }

    @Test
    public void clientShouldBeAbleListAllApplicationsWithToken() throws Exception {
        whenVerifyToken(USER_TOKEN);

        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1b = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);

                ListApplicationsResponse response =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(response, is(IsNull.notNullValue()));
                List<Application> applications = response.getApplications();
                assertThat(applications, is(IsNull.notNullValue()));
                assertThat(applications.size(), is(2));

                Application appX = applications.get(0);
                Application appY = applications.get(1);
                // Order might change
                if ("app1a".equals(appX.getName())) {
                    assertApplication(appX, "123", "app1a", "dev", "1.0.1-SNAPSHOT");
                    assertApplication(appY, "456", "app1b", "prod", "1.0.0");
                } else {
                    assertApplication(appY, "123", "app1a", "dev", "1.0.1-SNAPSHOT");
                    assertApplication(appX, "456", "app1b", "prod", "1.0.0");
                }
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleListApplicationsByAppName() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1b = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);


                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);
                listApplicationsRequest.setApplicationNames(Arrays.asList("app1a"));

                ListApplicationsResponse response =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(response, is(IsNull.notNullValue()));
                List<Application> applications = response.getApplications();
                assertThat(applications, is(IsNull.notNullValue()));
                assertThat(applications.size(), is(1));

                Application app1a = applications.get(0);
                assertApplication(app1a, "123", "app1a", "dev", "1.0.1-SNAPSHOT");
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleListApplicationsByAppVersion() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1b = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);
                listApplicationsRequest.setApplicationVersions(Arrays.asList("1.0.1-SNAPSHOT"));

                ListApplicationsResponse response =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(response, is(IsNull.notNullValue()));
                List<Application> applications = response.getApplications();
                assertThat(applications, is(IsNull.notNullValue()));
                assertThat(applications.size(), is(1));

                Application app1a = applications.get(0);
                assertApplication(app1a, "123", "app1a", "dev", "1.0.1-SNAPSHOT");
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldNotBeAbleListApplicationsWhenNoFilterMatches() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1b = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);
                listApplicationsRequest.setApplicationNames(Arrays.asList("xyz"));

                ListApplicationsResponse response =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(response, is(IsNull.notNullValue()));
                List<Application> applications = response.getApplications();
                assertThat(applications, is(IsNull.notNullValue()));
                assertThat(applications.size(), is(0));
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void applicationsShouldBeAbleToUpdated() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app", "prod", "1.0.0"));
                assertConnected(webSocketAppClient);

                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);

                ListApplicationsResponse responseBefore =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(responseBefore, is(IsNull.notNullValue()));

                List<Application> applicationsBefore = responseBefore.getApplications();
                assertThat(applicationsBefore, is(IsNull.notNullValue()));
                assertThat(applicationsBefore.size(), is(1));

                Application appBefore = applicationsBefore.get(0);
                assertApplication(appBefore, "123", "app", "prod", "1.0.0");
                assertEquals(appBefore.getTracePoints(), Collections.emptyList());

                ApplicationStatusEvent event = new ApplicationStatusEvent();
                event.setId(UUID.randomUUID().toString());
                Application app = new Application();
                app.setInstanceId("123");
                app.setName("app");
                app.setStage("prod");
                app.setVersion("1.0.0");
                TracePoint tracePoint1 = new TracePoint();
                tracePoint1.setFileName("com/mycompany/MyService.java");
                tracePoint1.setLineNo(10);
                tracePoint1.setClient(CLIENT);
                app.setTracePoints(Arrays.asList(tracePoint1));

                LogPoint logPoint1 = new LogPoint();
                logPoint1.setFileName("com/mycompany/MyService.java");
                logPoint1.setLineNo(11);
                logPoint1.setClient(CLIENT);
                app.setLogPoints(Arrays.asList(logPoint1));
                event.setApplication(app);

                webSocketAppClient.sendEventSync(event);

                String requestId2 = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest2 = new ListApplicationsRequest();
                listApplicationsRequest2.setId(requestId2);

                ListApplicationsResponse responseAfter =
                        webSocketUserClient.requestSync(listApplicationsRequest2, ListApplicationsResponse.class);

                assertThat(responseAfter, is(IsNull.notNullValue()));

                List<Application> applicationsAfter = responseAfter.getApplications();
                assertThat(applicationsAfter, is(IsNull.notNullValue()));
                assertThat(applicationsAfter.size(), is(1));

                Application appAfter = applicationsAfter.get(0);
                assertApplication(appAfter, "123", "app", "prod", "1.0.0");
                assertThat(appAfter.getTracePoints(), is(IsNull.notNullValue()));
                assertThat(appAfter.getTracePoints().size(), is(1));

                TracePoint tracePoint = appAfter.getTracePoints().get(0);
                assertTracePoint(tracePoint, "com/mycompany/MyService.java", 10, CLIENT);

                assertThat(appAfter.getLogPoints(), is(IsNull.notNullValue()));
                assertThat(appAfter.getLogPoints().size(), is(1));
                LogPoint logPoint = appAfter.getLogPoints().get(0);
                assertLogPoint(logPoint, "com/mycompany/MyService.java", 11, CLIENT);
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleNotifiedWhenAnApplicationIsNotAvailable() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            String requestId = UUID.randomUUID().toString();
            AppRequest appRequest = new AppRequest(Arrays.asList("testApp"));
            appRequest.setId(requestId);

            AppResponse response =
                    webSocketUserClient.requestSync(appRequest, AppResponse.class);

            assertThat(response, is(IsNull.notNullValue()));
            assertThat(response.getRequestId(), is(requestId));
            assertThat(response.getApplicationInstanceId(), is("testApp"));
            assertThat(response.isErroneous(), is(true));
            assertThat(response.getErrorCode(), is(ErrorCodes.TARGET_APPLICATION_NOT_AVAILABLE.getCode()));
        } finally {
            webSocketUserClient.close();
        }
    }

    private void exchangeDataBetweenAppAndClient(WebSocketClient webSocketUserClient,
                                                 String apiKey, String appInstanceId) {
        WebSocketClient webSocketAppClient = null;
        try {
            webSocketAppClient = new WebSocketClient(port, createAppCredentials(apiKey, appInstanceId));
            assertConnected(webSocketAppClient);

            TestRequest message = new TestRequest(Arrays.asList(appInstanceId));

            webSocketUserClient.send(message);

            TestRequest appMessage = webSocketAppClient.read(TestRequest.class, WAIT_SECS, TimeUnit.SECONDS);

            assertThat(appMessage, is(message));
        } finally {
            if (webSocketAppClient != null) {
                webSocketAppClient.close();
            }
        }
    }

    @Test
    public void sessionsShouldNotBeLeakedWhenToken() throws InterruptedException, Exception {
        final int USER_COUNT = 10;
        final int SESSION_COUNT = 10;

        whenVerifyToken(USER_TOKEN);

        final List<Thread> threads = new ArrayList<>();
        final List<Throwable> errors = new CopyOnWriteArrayList<>();

        final WebSocketClient webSocketClient = new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketClient);

            for (int i = 0; i < USER_COUNT; i++) {
                final int k = i;
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        for (int j = 0; j < SESSION_COUNT; j++) {
                            try {
                                final String appInstanceId = "App@" + k + "-" + j;
                                exchangeDataBetweenAppAndClient(webSocketClient, API_KEY, appInstanceId);
                            } catch (Throwable error) {
                                error.printStackTrace();
                                errors.add(error);
                            }
                        }
                    }
                };
                t.start();
                threads.add(t);
            }

            for (Thread t : threads) {
                t.join(TimeUnit.MINUTES.toMillis(1));
            }
        } finally {
            webSocketClient.close();
        }

        assertThat("There should not be errors: " + errors.toString(), errors.size(), is(0));
        for (int i = 0; i < 3; i++) {
            try {
                assertThat("Sessions should be cleaned", sessionService.getAllSessionMap().size(), is(0));
                break;
            } catch (AssertionError e) {
            }
            sleep(1000);
        }
        assertThat("Sessions should be cleaned", sessionService.getAllSessionMap().size(), is(0));
    }

    @Test
    public void clientShouldBeAbleListAllWebhooksWithToken() throws Exception {
        whenVerifyToken(USER_TOKEN);

        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            String requestId = UUID.randomUUID().toString();
            ListWebhooksRequest listWebhooksRequest = new ListWebhooksRequest();
            listWebhooksRequest.setId(requestId);

            ListWebhooksResponse response =
                    webSocketUserClient.requestSync(listWebhooksRequest, ListWebhooksResponse.class);

            assertThat(response, is(IsNull.notNullValue()));
            List<WebhookDto> webhooks = response.getWebhooks();
            assertThat(webhooks, is(IsNull.notNullValue()));
            assertThat(webhooks.size(), is(3));

            for (int i = 0; i < webhooks.size(); i++) {
                WebhookDto wh = webhooks.get(i);
                switch (wh.getName()) {
                    case "wh1":
                        assertWebhook(wh, "1", "wh1", WORKSPACE_ID, WebhookType.SLACK);
                        break;
                    case "wh2":
                        assertWebhook(wh, "2", "wh2", WORKSPACE_ID, WebhookType.CUSTOM);
                        break;
                    case "wh3":
                        assertWebhook(wh, "3", "wh3", WORKSPACE_ID, WebhookType.LOGZIO);
                        break;
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleListApplicationsByAppStage() {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1b = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                String requestId = UUID.randomUUID().toString();
                ListApplicationsRequest listApplicationsRequest = new ListApplicationsRequest();
                listApplicationsRequest.setId(requestId);
                listApplicationsRequest.setApplicationStages(Arrays.asList("prod"));

                ListApplicationsResponse response =
                        webSocketUserClient.requestSync(listApplicationsRequest, ListApplicationsResponse.class);

                assertThat(response, is(IsNull.notNullValue()));
                List<Application> applications = response.getApplications();
                assertThat(applications, is(IsNull.notNullValue()));
                assertThat(applications.size(), is(1));

                Application app1b = applications.get(0);
                assertApplication(app1b, "456", "app1b", "prod", "1.0.0");
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void clientShouldBeAbleToPutTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
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

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();

                Map<String, String> customTags = new HashMap<>();
                customTags.put("tag1", "tagValue2");
                customTags.put("tag2", "tagValue3");

                ApplicationFilter filter = new ApplicationFilter();
                filter.setCustomTags(customTags);
                filter.setName("app1a");
                filter.setStage("prod");

                ApplicationFilter filter2 = new ApplicationFilter();
                customTags.put("tag1", "tagValue1");
                filter2.setCustomTags(customTags);
                filter2.setName("app1a");

                filters.add(filter2);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture = registerForWaitingClientMessage(requestId);

                webSocketAppClient1a.clearReadMessages();
                webSocketAppClient2a.clearReadMessages();

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Put tracepoint request should be received by app1
                PutTracePointRequest receivedPutTracePointRequest =
                        webSocketAppClient1a.read(PutTracePointRequest.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedPutTracePointRequest);
                assertEquals(requestId, receivedPutTracePointRequest.getId());

                // Put tracepoint request should not be received by app2
                PutTracePointRequest receivedPutTracePointRequest2 =
                        webSocketAppClient2a.read(PutTracePointRequest.class, 3, TimeUnit.SECONDS);
                assertNull(receivedPutTracePointRequest2);

                PutTracePointResponse putTracePointResponse = new PutTracePointResponse();
                putTracePointResponse.setRequestId(requestId);
                putTracePointResponse.setErroneous(false);
                putTracePointResponse.setClient(CLIENT);

                webSocketUserClient.clearReadMessages();

                webSocketAppClient1a.send(putTracePointResponse);

                // Put tracepoint response should not be received by client
                PutTracePointResponse receivedPutTracePointResponse =
                        webSocketUserClient.read(PutTracePointResponse.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedPutTracePointResponse);
                assertEquals(requestId, receivedPutTracePointResponse.getRequestId());

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);
                assertNotNull(listTracePointResponse.getTracePoints());

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertEquals(tp.getConditionExpression(), "test == 1");
                assertEquals(tp.getExpireCount(), 1);
                assertEquals(tp.getExpireSecs(), 1800);
                assertTrue(tp.isTracingEnabled());

                ApplicationFilter applicationFilter = new ApplicationFilter();
                applicationFilter.setName("app1a");
                applicationFilter.setStage("dev");
                applicationFilter.setCustomTags(new HashMap<String, String>() {{
                    put("tag1", "tagValue1");
                    put("tag2", "tagValue3");
                }});

                Collection<TracePoint> tracePoints =
                        tracePointService.queryTracePoints(WORKSPACE_ID, applicationFilter);
                assertEquals(1, tracePoints.size());
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
    public void clientShouldNotBeAbleToPutTracePointWithSameId() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                putTracePointRequest.setId(requestId2);
                putTracePointRequest.setConditionExpression("test == 2");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(-1);
                putTracePointRequest.setExpireSecs(1);
                putTracePointRequest.setEnableTracing(false);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);
                assertNotNull(listTracePointResponse.getTracePoints());

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertEquals(tp.getConditionExpression(), "test == 1");
                assertEquals(tp.getExpireCount(), 1);
                assertEquals(tp.getExpireSecs(), 1800);
                assertTrue(tp.isTracingEnabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void appShouldBeAbleToQueryTracePoints() throws InterruptedException,
            ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1aProd = null;
            WebSocketClient webSocketAppClient1aVersion = null;
            WebSocketClient webSocketAppClient1b = null;
            WebSocketClient webSocketAppClient2a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1aProd =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "234",
                                        "app1a", "prod", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1aProd);


                webSocketAppClient1aVersion =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "345",
                                        "app1a", "dev", "1.0.0"));
                assertConnected(webSocketAppClient1aVersion);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                webSocketAppClient2a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "567",
                                        "app2a", "prod", "1.0.0"));
                assertConnected(webSocketAppClient2a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter1 = new ApplicationFilter();
                filter1.setName("app1a");
                filter1.setStage("dev");
                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag", "customVal1");
                }});
                filter1.setVersion("1.0.0");

                filters.add(filter1);

                // Create new tracePoint
                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Add another tracePoint with different filters
                String requestId2 = UUID.randomUUID().toString();
                putTracePointRequest.setId(requestId2);
                putTracePointRequest.setFileName("Test2.java");

                filters = new ArrayList<>();
                filter1.setName("app1a");
                filter1.setStage("dev");
                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag2", "customVal2");
                }});
                filter1.setVersion("1.0.0-SNAPSHOT");

                filters.add(filter1);
                putTracePointRequest.setApplicationFilters(filters);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag", "customVal1");
                    put("customTag2", "customVal2");
                }});
                Collection<TracePoint> tracePoints =
                        tracePointService.queryTracePoints(WORKSPACE_ID, filter1);

                assertNotNull(tracePoints);
                assertEquals(1, tracePoints.size());

                TracePoint tp = new ArrayList<>(tracePoints).get(0);

                assertEquals(tp.getFileName(), "Test2.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertEquals(tp.getConditionExpression(), "test == 1");
                assertEquals(tp.getExpireCount(), 1);
                assertEquals(tp.getExpireSecs(), 1800);
                assertTrue(tp.isTracingEnabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1aVersion != null) {
                    webSocketAppClient1aVersion.close();
                }
                if (webSocketAppClient1aProd != null) {
                    webSocketAppClient1aProd.close();
                }
                if (webSocketAppClient2a != null) {
                    webSocketAppClient2a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToUpdateTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                UpdateTracePointRequest updateTracePointRequest = new UpdateTracePointRequest();
                updateTracePointRequest.setId(requestId2);
                updateTracePointRequest.setConditionExpression("test == 2");
                updateTracePointRequest.setExpireCount(10);
                updateTracePointRequest.setExpireSecs(30);
                updateTracePointRequest.setEnableTracing(false);
                updateTracePointRequest.setTracePointId(tp.getId());
                updateTracePointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(updateTracePointRequest, UpdateTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                tp = listTracePointResponse.getTracePoints().get(0);
                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertEquals(tp.getConditionExpression(), "test == 2");
                assertEquals(tp.getExpireCount(), 10);
                assertEquals(tp.getExpireSecs(), 30);
                assertFalse(tp.isTracingEnabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToRemoveTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                RemoveTracePointRequest removeTracePointRequest = new RemoveTracePointRequest();
                removeTracePointRequest.setId(requestId2);
                removeTracePointRequest.setTracePointId(tp.getId());
                removeTracePointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(removeTracePointRequest, RemoveTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);
                assertTrue(listTracePointResponse.getTracePoints().isEmpty());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToDisableTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                DisableTracePointRequest disableTracePointRequest = new DisableTracePointRequest();
                disableTracePointRequest.setId(requestId2);
                disableTracePointRequest.setTracePointId(tp.getId());
                disableTracePointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(disableTracePointRequest, DisableTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                tp = listTracePointResponse.getTracePoints().get(0);
                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertTrue(tp.isDisabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToEnableTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                TracePoint tp = listTracePointResponse.getTracePoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                DisableTracePointRequest disableTracePointRequest = new DisableTracePointRequest();
                disableTracePointRequest.setId(requestId2);
                disableTracePointRequest.setTracePointId(tp.getId());
                disableTracePointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(disableTracePointRequest, DisableTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId3 = UUID.randomUUID().toString();

                EnableTracePointRequest enableTracePointRequest = new EnableTracePointRequest();
                enableTracePointRequest.setId(requestId3);
                enableTracePointRequest.setTracePointId(tp.getId());
                enableTracePointRequest.setPersist(true);

                CompletableFuture completableFuture3 = registerForWaitingClientMessage(requestId3);

                webSocketUserClient.request(enableTracePointRequest, EnableTracePointResponse.class);

                completableFuture3.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);

                tp = listTracePointResponse.getTracePoints().get(0);
                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertFalse(tp.isDisabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void cleanExpiredTracePoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                String requestId1 = UUID.randomUUID().toString();

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                PutTracePointRequest putTracePointRequest2 = new PutTracePointRequest();
                putTracePointRequest2.setId(requestId2);
                putTracePointRequest2.setFileName("Test.java");
                putTracePointRequest2.setClient(CLIENT);
                putTracePointRequest2.setLineNo(15);
                putTracePointRequest2.setPersist(true);
                putTracePointRequest2.setConditionExpression("test == 1");
                putTracePointRequest2.setApplicationFilters(filters);
                putTracePointRequest2.setExpireCount(1);
                putTracePointRequest2.setExpireSecs(3);
                putTracePointRequest2.setEnableTracing(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putTracePointRequest2, PutTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);
                assertNotNull(listTracePointResponse.getTracePoints());
                assertEquals(2, listTracePointResponse.getTracePoints().size());

                assertEventually(() -> {
                    ListTracePointsResponse listTracePointResponse3 = webSocketUserClient.requestSync(
                            listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listTracePointResponse3);
                    assertNotNull(listTracePointResponse3.getTracePoints());
                    assertEquals(0, listTracePointResponse3.getTracePoints().size());
                });
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void cleanExpiredTracePointKeepOther() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                String requestId1 = UUID.randomUUID().toString();

                PutTracePointRequest putTracePointRequest = new PutTracePointRequest();
                putTracePointRequest.setId(requestId1);
                putTracePointRequest.setFileName("Test.java");
                putTracePointRequest.setClient(CLIENT);
                putTracePointRequest.setLineNo(10);
                putTracePointRequest.setPersist(true);
                putTracePointRequest.setConditionExpression("test == 1");
                putTracePointRequest.setApplicationFilters(filters);
                putTracePointRequest.setExpireCount(1);
                putTracePointRequest.setExpireSecs(-1);
                putTracePointRequest.setEnableTracing(true);

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putTracePointRequest, PutTracePointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                PutTracePointRequest putTracePointRequest2 = new PutTracePointRequest();
                putTracePointRequest2.setId(requestId2);
                putTracePointRequest2.setFileName("Test.java");
                putTracePointRequest2.setClient(CLIENT);
                putTracePointRequest2.setLineNo(15);
                putTracePointRequest2.setPersist(true);
                putTracePointRequest2.setConditionExpression("test == 1");
                putTracePointRequest2.setApplicationFilters(filters);
                putTracePointRequest2.setExpireCount(1);
                putTracePointRequest2.setExpireSecs(2);
                putTracePointRequest2.setEnableTracing(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putTracePointRequest2, PutTracePointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListTracePointsRequest listTracePointRequest = new ListTracePointsRequest();
                listTracePointRequest.setId(UUID.randomUUID().toString());

                ListTracePointsResponse listTracePointResponse = webSocketUserClient.requestSync(
                        listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listTracePointResponse);
                assertNotNull(listTracePointResponse.getTracePoints());
                assertEquals(2, listTracePointResponse.getTracePoints().size());

                assertEventually(() -> {
                    ListTracePointsResponse listTracePointResponse1 = webSocketUserClient.requestSync(
                            listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listTracePointResponse1);
                    assertNotNull(listTracePointResponse1.getTracePoints());
                    assertEquals(2, listTracePointResponse1.getTracePoints().size());
                });

                assertEventually(() -> {
                    ListTracePointsResponse listTracePointResponse2 = webSocketUserClient.requestSync(
                            listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listTracePointResponse2);
                    assertNotNull(listTracePointResponse2.getTracePoints());
                    assertEquals(1, listTracePointResponse2.getTracePoints().size());
                });

                assertEventually(() -> {
                    ListTracePointsResponse listTracePointResponse3 = webSocketUserClient.requestSync(
                            listTracePointRequest, ListTracePointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listTracePointResponse3);
                    assertNotNull(listTracePointResponse3.getTracePoints());
                    assertEquals(1, listTracePointResponse3.getTracePoints().size());
                });
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void clientShouldBeAbleToPutLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
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

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();

                Map<String, String> customTags = new HashMap<>();
                customTags.put("tag1", "tagValue2");
                customTags.put("tag2", "tagValue3");

                ApplicationFilter filter = new ApplicationFilter();
                filter.setCustomTags(customTags);
                filter.setName("app1a");
                filter.setStage("prod");

                ApplicationFilter filter2 = new ApplicationFilter();
                customTags.put("tag1", "tagValue1");
                filter2.setCustomTags(customTags);
                filter2.setName("app1a");

                filters.add(filter2);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");
                putLogPointRequest.setStdoutEnabled(true);
                putLogPointRequest.setLogLevel("INFO");

                CompletableFuture completableFuture = registerForWaitingClientMessage(requestId);

                webSocketAppClient1a.clearReadMessages();
                webSocketAppClient2a.clearReadMessages();

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Put logpoint request should be received by app1
                PutLogPointRequest receivedPutLogPointRequest =
                        webSocketAppClient1a.read(PutLogPointRequest.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedPutLogPointRequest);
                assertEquals(requestId, receivedPutLogPointRequest.getId());

                // Put logpoint request should not be received by app2
                PutLogPointRequest receivedPutLogPointRequest2 =
                        webSocketAppClient2a.read(PutLogPointRequest.class, 3, TimeUnit.SECONDS);
                assertNull(receivedPutLogPointRequest2);

                PutLogPointResponse putLogPointResponse = new PutLogPointResponse();
                putLogPointResponse.setRequestId(requestId);
                putLogPointResponse.setErroneous(false);
                putLogPointResponse.setClient(CLIENT);

                webSocketUserClient.clearReadMessages();

                webSocketAppClient1a.send(putLogPointResponse);

                // Put logpoint response should not be received by client
                PutLogPointResponse receivedPutLogPointResponse =
                        webSocketUserClient.read(PutLogPointResponse.class, 30, TimeUnit.SECONDS);
                assertNotNull(receivedPutLogPointResponse);
                assertEquals(requestId, receivedPutLogPointResponse.getRequestId());

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);
                assertNotNull(listLogPointResponse.getLogPoints());

                LogPoint lp = listLogPointResponse.getLogPoints().get(0);

                assertEquals(lp.getFileName(), "Test.java");
                assertEquals(lp.getClient(), CLIENT);
                assertEquals(lp.getLineNo(), 10);
                assertEquals(lp.getConditionExpression(), "test == 1");
                assertEquals(lp.getExpireCount(), 1);
                assertEquals(lp.getExpireSecs(), 1800);
                assertEquals(lp.getLogExpression(), "test");
                assertTrue(lp.isStdoutEnabled());
                assertEquals(lp.getLogLevel(), "INFO");

                ApplicationFilter applicationFilter = new ApplicationFilter();
                applicationFilter.setName("app1a");
                applicationFilter.setStage("dev");
                applicationFilter.setCustomTags(new HashMap<String, String>() {{
                    put("tag1", "tagValue1");
                    put("tag2", "tagValue3");
                }});

                Collection<LogPoint> logPoints =
                        logPointService.queryLogPoints(WORKSPACE_ID, applicationFilter);
                assertEquals(1, logPoints.size());
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
    public void clientShouldNotBeAbleToPutLogPointWithSameId() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");
                putLogPointRequest.setStdoutEnabled(true);
                putLogPointRequest.setLogLevel("INFO");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                putLogPointRequest.setId(requestId2);
                putLogPointRequest.setConditionExpression("test == 2");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(-1);
                putLogPointRequest.setExpireSecs(1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);
                assertNotNull(listLogPointResponse.getLogPoints());

                LogPoint lp = listLogPointResponse.getLogPoints().get(0);

                assertEquals(lp.getFileName(), "Test.java");
                assertEquals(lp.getClient(), CLIENT);
                assertEquals(lp.getLineNo(), 10);
                assertEquals(lp.getConditionExpression(), "test == 1");
                assertEquals(lp.getExpireCount(), 1);
                assertEquals(lp.getExpireSecs(), 1800);
                assertEquals(lp.getLogExpression(), "test");
                assertTrue(lp.isStdoutEnabled());
                assertEquals(lp.getLogLevel(), "INFO");
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void appShouldBeAbleToQueryLogPoints() throws InterruptedException,
            ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            WebSocketClient webSocketAppClient1aProd = null;
            WebSocketClient webSocketAppClient1aVersion = null;
            WebSocketClient webSocketAppClient1b = null;
            WebSocketClient webSocketAppClient2a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                webSocketAppClient1aProd =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "234",
                                        "app1a", "prod", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1aProd);


                webSocketAppClient1aVersion =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "345",
                                        "app1a", "dev", "1.0.0"));
                assertConnected(webSocketAppClient1aVersion);

                webSocketAppClient1b =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "456",
                                        "app1b", "prod", "1.0.0"));
                assertConnected(webSocketAppClient1b);

                webSocketAppClient2a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "567",
                                        "app2a", "prod", "1.0.0"));
                assertConnected(webSocketAppClient2a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter1 = new ApplicationFilter();
                filter1.setName("app1a");
                filter1.setStage("dev");
                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag", "customVal1");
                }});
                filter1.setVersion("1.0.0");

                filters.add(filter1);

                // Create new logPoint
                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");
                putLogPointRequest.setStdoutEnabled(true);
                putLogPointRequest.setLogLevel("INFO");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Add another logPoint with different filters
                String requestId2 = UUID.randomUUID().toString();
                putLogPointRequest.setId(requestId2);
                putLogPointRequest.setFileName("Test2.java");

                filters = new ArrayList<>();
                filter1.setName("app1a");
                filter1.setStage("dev");
                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag2", "customVal2");
                }});
                filter1.setVersion("1.0.0-SNAPSHOT");

                filters.add(filter1);
                putLogPointRequest.setApplicationFilters(filters);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                filter1.setCustomTags(new HashMap<String, String>() {{
                    put("customTag", "customVal1");
                    put("customTag2", "customVal2");
                }});
                Collection<LogPoint> logPoints =
                        logPointService.queryLogPoints(WORKSPACE_ID, filter1);

                assertNotNull(logPoints);
                assertEquals(1, logPoints.size());

                LogPoint lp = new ArrayList<>(logPoints).get(0);

                assertEquals(lp.getFileName(), "Test2.java");
                assertEquals(lp.getClient(), CLIENT);
                assertEquals(lp.getLineNo(), 10);
                assertEquals(lp.getConditionExpression(), "test == 1");
                assertEquals(lp.getExpireCount(), 1);
                assertEquals(lp.getExpireSecs(), 1800);
                assertEquals(lp.getLogExpression(), "test");
                assertTrue(lp.isStdoutEnabled());
                assertEquals(lp.getLogLevel(), "INFO");
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
                if (webSocketAppClient1aVersion != null) {
                    webSocketAppClient1aVersion.close();
                }
                if (webSocketAppClient1aProd != null) {
                    webSocketAppClient1aProd.close();
                }
                if (webSocketAppClient2a != null) {
                    webSocketAppClient2a.close();
                }
                if (webSocketAppClient1b != null) {
                    webSocketAppClient1b.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToUpdateLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");
                putLogPointRequest.setStdoutEnabled(true);
                putLogPointRequest.setLogLevel("INFO");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                LogPoint lp = listLogPointResponse.getLogPoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                UpdateLogPointRequest updateLogPointRequest = new UpdateLogPointRequest();
                updateLogPointRequest.setId(requestId2);
                updateLogPointRequest.setConditionExpression("test == 2");
                updateLogPointRequest.setExpireCount(10);
                updateLogPointRequest.setExpireSecs(30);
                updateLogPointRequest.setLogPointId(lp.getId());
                updateLogPointRequest.setPersist(true);
                updateLogPointRequest.setLogExpression("testUpdated");
                putLogPointRequest.setStdoutEnabled(false);
                putLogPointRequest.setLogLevel(null);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(updateLogPointRequest, UpdateLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                lp = listLogPointResponse.getLogPoints().get(0);
                assertEquals(lp.getFileName(), "Test.java");
                assertEquals(lp.getClient(), CLIENT);
                assertEquals(lp.getLineNo(), 10);
                assertEquals(lp.getConditionExpression(), "test == 2");
                assertEquals(lp.getExpireCount(), 10);
                assertEquals(lp.getExpireSecs(), 30);
                assertEquals(lp.getLogExpression(), "testUpdated");
                assertFalse(lp.isStdoutEnabled());
                assertNull(lp.getLogLevel());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToRemoveLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                LogPoint lp = listLogPointResponse.getLogPoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();
                RemoveLogPointRequest removeLogPointRequest = new RemoveLogPointRequest();
                removeLogPointRequest.setId(requestId2);
                removeLogPointRequest.setLogPointId(lp.getId());
                removeLogPointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(removeLogPointRequest, RemoveLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);
                assertTrue(listLogPointResponse.getLogPoints().isEmpty());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToDisableLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                LogPoint tp = listLogPointResponse.getLogPoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                DisableLogPointRequest disableLogPointRequest = new DisableLogPointRequest();
                disableLogPointRequest.setId(requestId2);
                disableLogPointRequest.setLogPointId(tp.getId());
                disableLogPointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(disableLogPointRequest, DisableLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                tp = listLogPointResponse.getLogPoints().get(0);
                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertTrue(tp.isDisabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void clientShouldBeAbleToEnableLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient1a = null;
            try {
                webSocketAppClient1a =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT"));
                assertConnected(webSocketAppClient1a);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId1 = UUID.randomUUID().toString();
                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                LogPoint tp = listLogPointResponse.getLogPoints().get(0);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                DisableLogPointRequest disableLogPointRequest = new DisableLogPointRequest();
                disableLogPointRequest.setId(requestId2);
                disableLogPointRequest.setLogPointId(tp.getId());
                disableLogPointRequest.setPersist(true);

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(disableLogPointRequest, DisableLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId3 = UUID.randomUUID().toString();

                EnableLogPointRequest enableLogPointRequest = new EnableLogPointRequest();
                enableLogPointRequest.setId(requestId3);
                enableLogPointRequest.setLogPointId(tp.getId());
                enableLogPointRequest.setPersist(true);

                CompletableFuture completableFuture3 = registerForWaitingClientMessage(requestId3);

                webSocketUserClient.request(enableLogPointRequest, EnableLogPointResponse.class);

                completableFuture3.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);

                tp = listLogPointResponse.getLogPoints().get(0);
                assertEquals(tp.getFileName(), "Test.java");
                assertEquals(tp.getClient(), CLIENT);
                assertEquals(tp.getLineNo(), 10);
                assertFalse(tp.isDisabled());
            } finally {
                if (webSocketAppClient1a != null) {
                    webSocketAppClient1a.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void cleanExpiredLogPoint() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                String requestId1 = UUID.randomUUID().toString();

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                PutLogPointRequest putLogPointRequest2 = new PutLogPointRequest();
                putLogPointRequest2.setId(requestId2);
                putLogPointRequest2.setFileName("Test.java");
                putLogPointRequest2.setClient(CLIENT);
                putLogPointRequest2.setLineNo(15);
                putLogPointRequest2.setPersist(true);
                putLogPointRequest2.setConditionExpression("test == 1");
                putLogPointRequest2.setApplicationFilters(filters);
                putLogPointRequest2.setExpireCount(1);
                putLogPointRequest2.setExpireSecs(3);
                putLogPointRequest2.setLogExpression("test");

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putLogPointRequest2, PutLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);
                assertNotNull(listLogPointResponse.getLogPoints());
                assertEquals(2, listLogPointResponse.getLogPoints().size());

                assertEventually(() -> {
                    ListLogPointsResponse listLogPointResponse3 = webSocketUserClient.requestSync(
                            listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listLogPointResponse3);
                    assertNotNull(listLogPointResponse3.getLogPoints());
                    assertEquals(0, listLogPointResponse3.getLogPoints().size());
                });
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }

    @Test
    public void cleanExpiredLogPointKeepOther() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient webSocketUserClient =
                new WebSocketClient(port, createClientTokenCredentials(USER_TOKEN));
        try {
            assertConnected(webSocketUserClient);

            WebSocketClient webSocketAppClient = null;
            try {
                webSocketAppClient =
                        new WebSocketClient(
                                port,
                                createAppCredentials(
                                        API_KEY, "123",
                                        "app1a", "dev", "1.0.1-SNAPSHOT",
                                        new HashMap<String, String>() {{
                                            put("tag1", "tagValue1");
                                            put("tag2", "tagValue3");
                                        }}));
                assertConnected(webSocketAppClient);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                List<ApplicationFilter> filters = new ArrayList<>();
                ApplicationFilter filter = new ApplicationFilter();
                filter.setName("app1a");
                filters.add(filter);

                String requestId1 = UUID.randomUUID().toString();

                PutLogPointRequest putLogPointRequest = new PutLogPointRequest();
                putLogPointRequest.setId(requestId1);
                putLogPointRequest.setFileName("Test.java");
                putLogPointRequest.setClient(CLIENT);
                putLogPointRequest.setLineNo(10);
                putLogPointRequest.setPersist(true);
                putLogPointRequest.setConditionExpression("test == 1");
                putLogPointRequest.setApplicationFilters(filters);
                putLogPointRequest.setExpireCount(1);
                putLogPointRequest.setExpireSecs(-1);
                putLogPointRequest.setLogExpression("test");

                CompletableFuture completableFuture1 = registerForWaitingClientMessage(requestId1);

                webSocketUserClient.request(putLogPointRequest, PutLogPointResponse.class);

                completableFuture1.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String requestId2 = UUID.randomUUID().toString();

                PutLogPointRequest putLogPointRequest2 = new PutLogPointRequest();
                putLogPointRequest2.setId(requestId2);
                putLogPointRequest2.setFileName("Test.java");
                putLogPointRequest2.setClient(CLIENT);
                putLogPointRequest2.setLineNo(15);
                putLogPointRequest2.setPersist(true);
                putLogPointRequest2.setConditionExpression("test == 1");
                putLogPointRequest2.setApplicationFilters(filters);
                putLogPointRequest2.setExpireCount(1);
                putLogPointRequest2.setExpireSecs(2);
                putLogPointRequest2.setLogExpression("test");

                CompletableFuture completableFuture2 = registerForWaitingClientMessage(requestId2);

                webSocketUserClient.request(putLogPointRequest2, PutLogPointResponse.class);

                completableFuture2.get(30, TimeUnit.SECONDS);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                ListLogPointsRequest listLogPointRequest = new ListLogPointsRequest();
                listLogPointRequest.setId(UUID.randomUUID().toString());

                ListLogPointsResponse listLogPointResponse = webSocketUserClient.requestSync(
                        listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);

                assertNotNull(listLogPointResponse);
                assertNotNull(listLogPointResponse.getLogPoints());
                assertEquals(2, listLogPointResponse.getLogPoints().size());

                assertEventually(() -> {
                    ListLogPointsResponse listLogPointResponse1 = webSocketUserClient.requestSync(
                            listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listLogPointResponse1);
                    assertNotNull(listLogPointResponse1.getLogPoints());
                    assertEquals(2, listLogPointResponse1.getLogPoints().size());
                });

                assertEventually(() -> {
                    ListLogPointsResponse listLogPointResponse2 = webSocketUserClient.requestSync(
                            listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listLogPointResponse2);
                    assertNotNull(listLogPointResponse2.getLogPoints());
                    assertEquals(1, listLogPointResponse2.getLogPoints().size());
                });

                assertEventually(() -> {
                    ListLogPointsResponse listLogPointResponse3 = webSocketUserClient.requestSync(
                            listLogPointRequest, ListLogPointsResponse.class, 30, TimeUnit.SECONDS);
                    assertNotNull(listLogPointResponse3);
                    assertNotNull(listLogPointResponse3.getLogPoints());
                    assertEquals(1, listLogPointResponse3.getLogPoints().size());
                });
            } finally {
                if (webSocketAppClient != null) {
                    webSocketAppClient.close();
                }
            }
        } finally {
            webSocketUserClient.close();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class AppRequest extends BaseRequest {

        private final List<String> applications;

        private AppRequest(List<String> applications) {
            this.applications = applications;
        }

        public List<String> getApplications() {
            return applications;
        }

    }

    private static class AppResponse extends BaseApplicationResponse {

    }

}
