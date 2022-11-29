package com.runsidekick.broker.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runsidekick.broker.exception.RequestTimeoutException;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.response.impl.ApplicationAwareProbeResponse;
import com.runsidekick.broker.model.response.impl.ApplicationAwareResponse;
import com.runsidekick.broker.model.response.impl.CompositeResponse;
import io.thundra.swark.utils.ExceptionUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class SidekickBrokerClient extends WebSocketListener {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final long READ_TIMEOUT = 30;
    private static final long SYNC_REQUEST_TIMEOUT = 10;
    private static final long REQUEST_TIMEOUT_MSECS = 30 * 1000; // 30 seconds
    private static final int CLOSE_TIMEOUT_MSEC = 1000;

    private final OkHttpClient client =
            new OkHttpClient.Builder().
                    readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).
                    pingInterval(Duration.ofMinutes(1)).
                    build();
    private final WebSocket webSocket;
    private final ClientCredentials clientCredentials;
    private final CountDownLatch connectedCountDownLatch = new CountDownLatch(1);
    private final CountDownLatch closedCountDownLatch = new CountDownLatch(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final Map<String, InFlightRequest> requestMap = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().
                    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final ConnectionCallback connectionCallback;

    public SidekickBrokerClient(int port, ClientCredentials clientCredentials) {
        this("localhost", port, clientCredentials);
    }

    public SidekickBrokerClient(String host, int port, ClientCredentials clientCredentials) {
        this(host, port, clientCredentials, null);
    }

    public SidekickBrokerClient(String host, int port, ClientCredentials clientCredentials,
                                ConnectionCallback connectionCallback) {
        this.clientCredentials = clientCredentials;
        this.connectionCallback = connectionCallback;
        Request request = buildClientRequest(host, port, clientCredentials);
        webSocket = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    private static Request buildClientRequest(String host, int port, ClientCredentials clientCredentials) {
        Request.Builder builder = new Request.Builder();
        if (host.startsWith("ws://") || host.startsWith("wss://")) {
            builder.url(host + ":" + port + "/api");
        } else {
            builder.url("wss://" + host + ":" + port + "/api");
        }
        builder.header("x-sidekick-token", clientCredentials.getToken());
        return builder.build();
    }

    private static String extractStringProp(JsonNode jsonNode, String prop) {
        try {
            if (jsonNode.has(prop)) {
                return jsonNode.get(prop).asText();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static List<String> extractArrayProp(JsonNode jsonNode, String prop) {
        try {
            if (jsonNode.has(prop) && jsonNode.get(prop).isArray()) {
                JsonNode arrNode = jsonNode.get(prop);
                List<String> arr = new ArrayList<>();
                for (final JsonNode objNode : arrNode) {
                    arr.add(objNode.asText());
                }
                return arr;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String extractRequestId(String message, String requestIdField) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(message);
            return extractStringProp(jsonNode, requestIdField);
        } catch (Exception e) {
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isConnected() {
        return connectedCountDownLatch.getCount() == 0;
    }

    public boolean waitUntilConnected() {
        try {
            connectedCountDownLatch.await();
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean waitUntilConnected(long timeout, TimeUnit unit) {
        try {
            return connectedCountDownLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void send(String message) {
        webSocket.send(message);
    }

    public void send(Object messageObj) {
        try {
            String message = OBJECT_MAPPER.writeValueAsString(messageObj);
            send(message);
        } catch (JsonProcessingException e) {
            ExceptionUtils.sneakyThrow(new IOException("Unable to serialize message object", e));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <R> CompletableFuture<R> doRequest(String request, Class<R> responseClass, long timeoutMsec) {
        CompletableFuture<R> completableFuture = new CompletableFuture();
        String requestId = extractRequestId(request, "id");
        if (requestId == null) {
            throw new IllegalArgumentException("Request must contain 'requestId'");
        }
        ScheduledFuture scheduledFuture =
                scheduledExecutorService.schedule(() -> {
                    InFlightRequest inFlightRequest = requestMap.remove(requestId);
                    if (inFlightRequest != null) {
                        if (inFlightRequest.completableFuture != null) {
                            inFlightRequest.completableFuture.completeExceptionally(
                                    new RequestTimeoutException(
                                            String.format("Request with id %s has timed-out", requestId)));
                        }
                    }
                }, timeoutMsec, TimeUnit.MILLISECONDS);
        requestMap.put(requestId, new InFlightRequest(completableFuture, scheduledFuture, responseClass, null));
        try {
            send(request);
        } catch (Throwable t) {
            requestMap.remove(requestId);
            scheduledFuture.cancel(true);
            ExceptionUtils.sneakyThrow(t);
        }
        return completableFuture;
    }

    private <R extends com.runsidekick.broker.model.response.Response> CompletableFuture<CompositeResponse<R>>
    doRequestAll(String request,
                 Class<R> responseClass,
                 long timeoutMsec,
                 List<ApplicationFilter> applicationFilters) {
        CompletableFuture<CompositeResponse<R>> completableFuture = new CompletableFuture();
        String requestId = extractRequestId(request, "id");
        if (requestId == null) {
            throw new IllegalArgumentException("Request must contain 'requestId'");
        }
        ScheduledFuture scheduledFuture =
                scheduledExecutorService.schedule(() -> {
                    InFlightRequest inFlightRequest = requestMap.remove(requestId);
                    if (inFlightRequest != null) {
                        if (inFlightRequest.completableFuture != null) {
                            inFlightRequest.completableFuture.completeExceptionally(
                                    new RequestTimeoutException(
                                            String.format("Request with id %s has timed-out", requestId)));
                        }
                    }
                }, timeoutMsec, TimeUnit.MILLISECONDS);
        requestMap.put(requestId,
                new InFlightRequest(completableFuture, scheduledFuture, responseClass, applicationFilters));
        try {
            send(request);
        } catch (Throwable t) {
            requestMap.remove(requestId);
            scheduledFuture.cancel(true);
            ExceptionUtils.sneakyThrow(t);
        }
        return completableFuture;
    }

    public CompletableFuture<String> request(String request) {
        return doRequest(request, String.class, REQUEST_TIMEOUT_MSECS);
    }

    public String requestSync(String request, long timeout, TimeUnit timeUnit) {
        try {
            return doRequest(request, String.class, timeUnit.toMillis(timeout)).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ExceptionUtils.sneakyThrow(new IOException("Request failed", e));
            return null;
        }
    }

    public String requestSync(String request) {
        return requestSync(request, SYNC_REQUEST_TIMEOUT, TimeUnit.SECONDS);
    }

    public <R> CompletableFuture<R> request(Object requestObj, Class<R> responseClass) {
        try {
            String request = OBJECT_MAPPER.writeValueAsString(requestObj);
            return doRequest(request, responseClass, REQUEST_TIMEOUT_MSECS);
        } catch (JsonProcessingException e) {
            ExceptionUtils.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        }
    }

    public <R> R requestSync(Object requestObj, Class<R> responseClass, long timeout, TimeUnit timeUnit) {
        try {
            String request = OBJECT_MAPPER.writeValueAsString(requestObj);
            return doRequest(request, responseClass, timeUnit.toMillis(timeout)).get(timeout, timeUnit);
        } catch (JsonProcessingException e) {
            ExceptionUtils.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ExceptionUtils.sneakyThrow(new IOException("Request failed", e));
            return null;
        }
    }

    public <R> R requestSync(Object request, Class<R> responseClass) {
        return requestSync(request, responseClass, SYNC_REQUEST_TIMEOUT, TimeUnit.SECONDS);
    }

    public <R extends com.runsidekick.broker.model.response.Response> CompositeResponse<R> requestSyncAll(
            Object request, Class<R> responseClass, List<ApplicationFilter> applicationFilters) {
        return requestSyncAll(request, responseClass, SYNC_REQUEST_TIMEOUT, TimeUnit.SECONDS, applicationFilters);
    }

    public <R extends com.runsidekick.broker.model.response.Response> CompletableFuture<CompositeResponse<R>>
    requestAll(Object requestObj, Class<R> responseClass) {
        return requestAll(requestObj, responseClass, null);
    }

    public <R extends com.runsidekick.broker.model.response.Response> CompletableFuture<CompositeResponse<R>>
    requestAll(Object requestObj,
               Class<R> responseClass,
               List<ApplicationFilter> applicationFilters) {
        try {
            String request = OBJECT_MAPPER.writeValueAsString(requestObj);
            return doRequestAll(request, responseClass, REQUEST_TIMEOUT_MSECS, applicationFilters);
        } catch (JsonProcessingException e) {
            ExceptionUtils.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        }
    }

    public <R extends com.runsidekick.broker.model.response.Response> CompositeResponse<R>
    requestSyncAll(Object requestObj, Class<R> responseClass,
                   long timeout, TimeUnit timeUnit,
                   List<ApplicationFilter> applicationFilters) {
        try {
            String request = OBJECT_MAPPER.writeValueAsString(requestObj);
            return doRequestAll(request, responseClass, timeUnit.toMillis(timeout), applicationFilters)
                    .get(timeout, timeUnit);
        } catch (JsonProcessingException e) {
            ExceptionUtils.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ExceptionUtils.sneakyThrow(new IOException("Request failed", e));
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void close() {
        try {
            webSocket.close(CLOSE_TIMEOUT_MSEC, null);
        } catch (Exception e) {
        }
    }

    public void closeAndWait() {
        try {
            webSocket.close(CLOSE_TIMEOUT_MSEC, null);
            waitUntilClosed();
        } catch (Exception e) {
        }
    }

    public boolean isClosed() {
        return closedCountDownLatch.getCount() == 0;
    }

    public boolean waitUntilClosed() {
        try {
            closedCountDownLatch.await();
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean waitUntilClosed(long timeout, TimeUnit unit) {
        try {
            return closedCountDownLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void destroy() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        logger.debug("OPEN: " + response.message());

        connectedCountDownLatch.countDown();
        if (null != connectionCallback) {
            connectionCallback.onConnectSuccess(this);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        logger.debug("MESSAGE: " + text);

        JsonNode jsonNode;
        try {
            jsonNode = OBJECT_MAPPER.readTree(text);
        } catch (Exception e) {
            logger.warn("Unable to parse message: " + text, e);
            return;
        }
        checkAndHandleInFlightRequests(text, jsonNode);
    }

    private void checkAndHandleInFlightRequests(String text, JsonNode jsonNode) {
        if (requestMap.size() > 0) {
            String requestId = extractStringProp(jsonNode, "requestId");
            if (requestId != null) {
                InFlightRequest inFlightRequest = requestMap.get(requestId);
                if (inFlightRequest != null) {
                    if (jsonNode.has("applicationInstanceIds")) {
                        //Broker response
                        List<String> applicationInstanceIds = extractArrayProp(jsonNode, "applicationInstanceIds");
                        try {
                            Object response = OBJECT_MAPPER.readValue(text, inFlightRequest.responseClass);
                            if (response instanceof ApplicationAwareProbeResponse) {
                                ApplicationAwareResponse applicationResponse = (ApplicationAwareResponse) response;
                                inFlightRequest.applicationResponseMap.put("-", applicationResponse);
                            } else if (response instanceof ApplicationAwareResponse) {
                                ApplicationAwareResponse applicationResponse = (ApplicationAwareResponse) response;
                                inFlightRequest.applicationResponseMap.put("-", applicationResponse);
                            }
                        } catch (JsonProcessingException e) {
                            inFlightRequest.completableFuture.completeExceptionally(
                                    new IOException("Unable to deserialize response: " + text, e));
                        }
                        if (applicationInstanceIds != null && applicationInstanceIds.size() > 0) {
                            for (String applicationInstanceId: applicationInstanceIds) {
                                inFlightRequest.applicationResponseMap.put(applicationInstanceId, new Object());
                            }
                        } else {
                            if (inFlightRequest.scheduledFuture != null) {
                                inFlightRequest.scheduledFuture.cancel(true);
                            }
                            CompositeResponse compositeResponse =
                                    new CompositeResponse(
                                            new ArrayList<>(inFlightRequest.applicationResponseMap.values()));
                            inFlightRequest.completableFuture.complete(compositeResponse);
                            requestMap.remove(requestId);
                        }
                    } else if (jsonNode.has("applicationInstanceId")) {
                        //Agent response
                        String applicationInstanceId = extractStringProp(jsonNode, "applicationInstanceId");
                        try {
                            if (inFlightRequest.containsApplicationInstance(applicationInstanceId)) {
                                Object response = OBJECT_MAPPER.readValue(text, inFlightRequest.responseClass);
                                if (response instanceof ApplicationAwareProbeResponse) {
                                    ApplicationAwareResponse applicationResponse = (ApplicationAwareResponse) response;
                                    inFlightRequest.applicationResponseMap.put(
                                            applicationInstanceId, applicationResponse);
                                } else if (response instanceof ApplicationAwareResponse) {
                                    ApplicationAwareResponse applicationResponse = (ApplicationAwareResponse) response;
                                    inFlightRequest.applicationResponseMap.put(
                                            applicationInstanceId, applicationResponse);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            inFlightRequest.applicationResponseMap.put(applicationInstanceId,
                                    new IOException("Unable to deserialize response: " + text, e));
                        }
                        if (inFlightRequest.isCompleted()) {
                            if (inFlightRequest.scheduledFuture != null) {
                                inFlightRequest.scheduledFuture.cancel(true);
                            }
                            CompositeResponse compositeResponse =
                                    new CompositeResponse(
                                            new ArrayList<>(inFlightRequest.applicationResponseMap.values()));
                            inFlightRequest.completableFuture.complete(compositeResponse);
                            requestMap.remove(requestId);
                        }
                    } else {
                        try {
                            Object response = OBJECT_MAPPER.readValue(text, inFlightRequest.responseClass);
                            inFlightRequest.completableFuture.complete(response);
                        } catch (IOException e) {
                            inFlightRequest.completableFuture.completeExceptionally(
                                    new IOException("Unable to deserialize response: " + text, e));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        logger.debug("MESSAGE: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        logger.debug("CLOSING: " + code + " " + reason);

        webSocket.close(CLOSE_TIMEOUT_MSEC, null);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        logger.debug("CLOSED: " + code + " " + reason);

        closedCountDownLatch.countDown();
        if (null != connectionCallback) {
            connectionCallback.onClose(this, null);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        logger.warn("FAILED: " + t.getMessage(), t);

        if (!isConnected()) {
            closedCountDownLatch.countDown();
            connectionCallback.onConnectFailure(this, t);
        } else {
            connectionCallback.onClose(this, t);
        }
    }

    private static final class InFlightRequest {

        private final CompletableFuture completableFuture;
        private final ScheduledFuture scheduledFuture;
        private final Class responseClass;
        private final Map<String, Object> applicationResponseMap;


        private InFlightRequest(CompletableFuture completableFuture,
                                ScheduledFuture scheduledFuture,
                                Class responseClass, List<ApplicationFilter> applicationFilters) {
            this.completableFuture = completableFuture;
            this.scheduledFuture = scheduledFuture;
            this.responseClass = responseClass;
            this.applicationResponseMap = new ConcurrentHashMap<>();
        }

        public int getCompletedInstanceCount() {
            return applicationResponseMap.values().stream()
                    .filter(response -> response instanceof IOException
                            || response instanceof ApplicationAwareProbeResponse)
                    .collect(Collectors.toList()).size();
        }

        public int getTotalInstanceCount() {
            return applicationResponseMap.size();
        }

        public boolean isCompleted() {
            return getCompletedInstanceCount() == getTotalInstanceCount();
        }

        public boolean containsApplicationInstance(String applicationInstanceId) {
            return applicationResponseMap.containsKey(applicationInstanceId);
        }

    }

    public interface ConnectionCallback {

        void onConnectSuccess(SidekickBrokerClient client);

        void onConnectFailure(SidekickBrokerClient client, Throwable t);

        void onClose(SidekickBrokerClient client, Throwable t);

    }

}
