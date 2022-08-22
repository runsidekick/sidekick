package com.runsidekick.broker.integration.setup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opsgenie.core.util.ExceptionUtil;
import com.runsidekick.broker.model.event.EventAck;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author serkan.ozal
 */
public final class WebSocketClient extends WebSocketListener {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketClient.class);

    private static final long REQUEST_TIMEOUT_MSECS = 30 * 1000; // 30 seconds
    private static final long EVENT_TIMEOUT_MSECS = 10 * 1000; // 10 seconds

    private final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1000);
    private final OkHttpClient client =
            new OkHttpClient.Builder().
                    readTimeout(3, TimeUnit.SECONDS).
                    build();
    private final WebSocket webSocket;
    private final CountDownLatch connectedCountDownLatch = new CountDownLatch(1);
    private final CountDownLatch closedCountDownLatch = new CountDownLatch(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final Map<String, InFlightRequest> requestMap = new ConcurrentHashMap<>();
    private final Map<String, InFlightEvent> eventMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper =
            new ObjectMapper().
                    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public WebSocketClient(int port, AppCredentials appCredentials) {
        this("localhost", port, appCredentials);
    }

    public WebSocketClient(String host, int port, AppCredentials appCredentials) {
        Request request = buildAppRequest(host, port, appCredentials);
        webSocket = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    public WebSocketClient(int port, ClientCredentials clientCredentials) {
        this("localhost", port, clientCredentials);
    }

    public WebSocketClient(String host, int port, ClientCredentials clientCredentials) {
        Request request = buildClientRequest(host, port, clientCredentials);
        webSocket = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    private static Request buildAppRequest(String host, int port, AppCredentials appCredentials) {
        Request.Builder builder = new Request.Builder();
        String url = host + ":" + port + "/app";
        if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            url = "ws://" + url;
        }
        builder.url(url);
        if (appCredentials.getApiKey() != null) {
            builder.header("x-sidekick-api-key", appCredentials.getApiKey());
        }
        if (appCredentials.getAppInstanceId() != null) {
            builder.header("x-sidekick-app-instance-id", appCredentials.getAppInstanceId());
        }
        if (appCredentials.getAppName() != null) {
            builder.header("x-sidekick-app-name", appCredentials.getAppName());
        }
        if (appCredentials.getAppStage() != null) {
            builder.header("x-sidekick-app-stage", appCredentials.getAppStage());
        }
        if (appCredentials.getAppVersion() != null) {
            builder.header("x-sidekick-app-version", appCredentials.getAppVersion());
        }
        if (appCredentials.getAppCustomTags() != null && !appCredentials.getAppCustomTags().isEmpty()) {
            appCredentials.getAppCustomTags().forEach((tagName, tagValue) -> {
                builder.header("x-sidekick-app-tag-" + tagName, tagValue);
            });
        }
        return builder.build();
    }

    private static Request buildClientRequest(String host, int port, ClientCredentials clientCredentials) {
        Request.Builder builder = new Request.Builder();
        String url = host + ":" + port + "/client";
        if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            url = "ws://" + url;
        }
        if (clientCredentials.isAuthenticateOverPath() && clientCredentials.isAuthenticateWithToken()) {
            url += "/" + clientCredentials.getToken();
            builder.url(url);
        } else {
            builder.url(url);
            if (clientCredentials.getToken() != null) {
                builder.header("x-sidekick-token", clientCredentials.getToken());
            }
        }
        return builder.build();
    }

    private static String extractMessageProp(String message, String propFieldField) {
        try {
            JSONObject messageObj = new JSONObject(message);
            if (messageObj.has(propFieldField)) {
                return messageObj.getString(propFieldField);
            }
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
            String message = objectMapper.writeValueAsString(messageObj);
            send(message);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to serialize message object", e));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <R> CompletableFuture<R> doRequest(String request, Class<R> responseClass, long timeoutMsec) {
        CompletableFuture<R> completableFuture = new CompletableFuture();
        String requestId = extractMessageProp(request, "id");
        if (requestId == null) {
            throw new IllegalArgumentException("Request must contain 'id'");
        }
        ScheduledFuture scheduledFuture =
                scheduledExecutorService.schedule(() -> {
                    InFlightRequest inFlightRequest = requestMap.remove(requestId);
                    if (inFlightRequest != null) {
                        if (inFlightRequest.completableFuture != null) {
                            inFlightRequest.completableFuture.completeExceptionally(
                                    new TimeoutException(
                                            String.format("Request with id %s has timed-out", requestId)));
                        }
                    }
                }, timeoutMsec, TimeUnit.MILLISECONDS);
        requestMap.put(requestId, new InFlightRequest(completableFuture, scheduledFuture, responseClass));
        try {
            send(request);
        } catch (Throwable t) {
            requestMap.remove(requestId);
            scheduledFuture.cancel(true);
            ExceptionUtil.sneakyThrow(t);
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
            LOGGER.error("Request failed", e);
            return null;
        }
    }

    public String requestSync(String request) {
        return requestSync(request, 3, TimeUnit.SECONDS);
    }

    public <R> CompletableFuture<R> request(Object requestObj, Class<R> responseClass) {
        try {
            String request = objectMapper.writeValueAsString(requestObj);
            return doRequest(request, responseClass, REQUEST_TIMEOUT_MSECS);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        }
    }

    public <R> R requestSync(Object requestObj, Class<R> responseClass, long timeout, TimeUnit timeUnit) {
        try {
            String request = objectMapper.writeValueAsString(requestObj);
            return doRequest(request, responseClass, timeUnit.toMillis(timeout)).get(timeout, timeUnit);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to serialize request", e));
            return null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Request failed", e);
            return null;
        }
    }

    public <R> R requestSync(Object requestObj, Class<R> responseClass) {
        return requestSync(requestObj, responseClass, 30, TimeUnit.SECONDS);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<EventAck> doSendEvent(String event, long timeoutMsec) {
        CompletableFuture<EventAck> completableFuture = new CompletableFuture();
        String eventId = extractMessageProp(event, "id");
        if (eventId == null) {
            throw new IllegalArgumentException("Event must contain 'id'");
        }
        ScheduledFuture scheduledFuture =
                scheduledExecutorService.schedule(() -> {
                    InFlightEvent inFlightEvent = eventMap.remove(eventId);
                    if (inFlightEvent != null) {
                        if (inFlightEvent.completableFuture != null) {
                            inFlightEvent.completableFuture.completeExceptionally(
                                    new TimeoutException(
                                            String.format("Event with id %s has timed-out", eventId)));
                        }
                    }
                }, timeoutMsec, TimeUnit.MILLISECONDS);
        eventMap.put(eventId, new InFlightEvent(completableFuture, scheduledFuture));
        try {
            send(event);
        } catch (Throwable t) {
            eventMap.remove(eventId);
            scheduledFuture.cancel(true);
            ExceptionUtil.sneakyThrow(t);
        }
        return completableFuture;
    }

    public CompletableFuture<EventAck> sendEvent(String event) {
        return doSendEvent(event, EVENT_TIMEOUT_MSECS);
    }

    public EventAck sendEventSync(String event, long timeout, TimeUnit timeUnit) {
        try {
            return doSendEvent(event, timeUnit.toMillis(timeout)).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Send event failed", e);
            return null;
        }
    }

    public EventAck sendEventSync(String event) {
        return sendEventSync(event, 3, TimeUnit.SECONDS);
    }

    public CompletableFuture<EventAck> sendEvent(Object eventObj) {
        try {
            String event = objectMapper.writeValueAsString(eventObj);
            return doSendEvent(event, EVENT_TIMEOUT_MSECS);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to serialize event", e));
            return null;
        }
    }

    public EventAck sendEventSync(Object eventObj, long timeout, TimeUnit timeUnit) {
        try {
            String event = objectMapper.writeValueAsString(eventObj);
            return doSendEvent(event, timeUnit.toMillis(timeout)).get(timeout, timeUnit);
        } catch (JsonProcessingException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to serialize event", e));
            return null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Send event failed", e);
            return null;
        }
    }

    public EventAck sendEventSync(Object eventObj) {
        return sendEventSync(eventObj, 30, TimeUnit.SECONDS);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String read() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public String read(long timeout, TimeUnit unit) {
        try {
            return messageQueue.poll(timeout, unit);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public <R> R read(Class<R> messageClass) {
        String message = read();
        if (message == null) {
            return null;
        }
        try {
            return objectMapper.readValue(message, messageClass);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to deserialize message: " + message, e));
            return null;
        }
    }

    public <R> R read(Class<R> messageClass, long timeout, TimeUnit unit) {
        String message = read(timeout, unit);
        if (message == null) {
            return null;
        }
        try {
            return objectMapper.readValue(message, messageClass);
        } catch (IOException e) {
            ExceptionUtil.sneakyThrow(new IOException("Unable to deserialize message: " + message, e));
            return null;
        }
    }

    public void clearReadMessages() {
        messageQueue.clear();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void close() {
        try {
            webSocket.close(1000, null);
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        LOGGER.info("OPEN: " + response.message());
        connectedCountDownLatch.countDown();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        LOGGER.info("MESSAGE: " + text);
        messageQueue.add(text);
        checkInFlightRequests(text);
        checkInFlightEvents(text);
    }

    private void checkInFlightRequests(String text) {
        if (requestMap.size() > 0) {
            String requestId = extractMessageProp(text, "requestId");
            if (requestId != null) {
                InFlightRequest inFlightRequest = requestMap.remove(requestId);
                if (inFlightRequest != null) {
                    if (inFlightRequest.scheduledFuture != null) {
                        inFlightRequest.scheduledFuture.cancel(true);
                    }
                    if (inFlightRequest.completableFuture != null) {
                        if (inFlightRequest.responseClass == null
                                || inFlightRequest.responseClass.equals(String.class)) {
                            inFlightRequest.completableFuture.complete(text);
                        } else {
                            try {
                                Object response = objectMapper.readValue(text, inFlightRequest.responseClass);
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
    }

    private void checkInFlightEvents(String text) {
        if (eventMap.size() > 0) {
            String eventId = extractMessageProp(text, "eventId");
            if (eventId != null) {
                InFlightEvent inFlightEvent = eventMap.remove(eventId);
                if (inFlightEvent != null) {
                    if (inFlightEvent.scheduledFuture != null) {
                        inFlightEvent.scheduledFuture.cancel(true);
                    }
                    if (inFlightEvent.completableFuture != null) {
                        try {
                            EventAck eventAck = objectMapper.readValue(text, EventAck.class);
                            inFlightEvent.completableFuture.complete(eventAck);
                        } catch (IOException e) {
                            inFlightEvent.completableFuture.completeExceptionally(
                                    new IOException("Unable to deserialize event ACK: " + text, e));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        LOGGER.info("MESSAGE: " + bytes.hex());
        messageQueue.add(bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        LOGGER.info("CLOSING: " + code + " " + reason);
        webSocket.close(1000, null);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        LOGGER.info("CLOSED: " + code + " " + reason);
        closedCountDownLatch.countDown();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        LOGGER.error("FAILED: " + t.getMessage(), t);
        if (!isConnected()) {
            closedCountDownLatch.countDown();
        }
    }

    private static class InFlightRequest {

        private final CompletableFuture completableFuture;
        private final ScheduledFuture scheduledFuture;
        private final Class responseClass;

        private InFlightRequest(CompletableFuture completableFuture,
                                ScheduledFuture scheduledFuture,
                                Class responseClass) {
            this.completableFuture = completableFuture;
            this.scheduledFuture = scheduledFuture;
            this.responseClass = responseClass;
        }

    }

    private static class InFlightEvent {

        private final CompletableFuture completableFuture;
        private final ScheduledFuture scheduledFuture;

        private InFlightEvent(CompletableFuture completableFuture,
                              ScheduledFuture scheduledFuture) {
            this.completableFuture = completableFuture;
            this.scheduledFuture = scheduledFuture;
        }

    }

}
