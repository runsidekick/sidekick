package com.runsidekick.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.runsidekick.model.EventType;
import com.runsidekick.model.PhoneHomeMetric;
import com.runsidekick.model.PhoneHomeMetricUtil;
import com.runsidekick.service.PhoneHomeMetricService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yasin.kalafat
 */
@Service
public class PhoneHomeMetricServiceImpl implements PhoneHomeMetricService {

    private static final Logger LOGGER = LogManager.getLogger(PhoneHomeMetricServiceImpl.class);

    @Value("${app.version:}")
    private String appVersion;

    @Value("${phone-home.url:}")
    private String phoneHomeUrl;

    private static final long READ_TIMEOUT = 30;
    private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writer();

    private final OkHttpClient client =
            new OkHttpClient.Builder().
                    readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).
                    pingInterval(Duration.ofMinutes(1)).
                    build();

    private PhoneHomeMetric phoneHomeMetric;

    @PostConstruct
    public void initPhoneHomeMetric() {
        phoneHomeMetric = PhoneHomeMetric.builder()
                .javaVersion(System.getProperty("java.version"))
                .appVersion(appVersion)
                .macAddress(PhoneHomeMetricUtil.getMacAddress())
                .build();
        PhoneHomeMetricUtil.setOsInfo(phoneHomeMetric);
        PhoneHomeMetricUtil.setHostName(phoneHomeMetric);
    }

    @Override
    public void sendServerUpEvent(long startTime) {
        try {
            PhoneHomeMetric metric = (PhoneHomeMetric) phoneHomeMetric.clone();
            metric.setEventType(EventType.SERVER_UP);

            Map<String, Object> eventDetails = new HashMap();
            eventDetails.put("startTime", startTime);
            metric.setEventDetails(eventDetails);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, OBJECT_WRITER.writeValueAsString(metric));
            Request request = new Request.Builder()
                    .url(phoneHomeUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    LOGGER.error(response);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        } catch (CloneNotSupportedException | JsonProcessingException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void sendServerDownEvent(long startTime, long finishTime) {
        try {
            PhoneHomeMetric metric = (PhoneHomeMetric) phoneHomeMetric.clone();
            metric.setEventType(EventType.SERVER_DOWN);

            Map<String, Object> eventDetails = new HashMap();
            eventDetails.put("startTime", startTime);
            eventDetails.put("finishTime", finishTime);
            metric.setEventDetails(eventDetails);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, OBJECT_WRITER.writeValueAsString(metric));
            Request request = new Request.Builder()
                    .url(phoneHomeUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    LOGGER.error(response);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        } catch (CloneNotSupportedException | JsonProcessingException e) {
            LOGGER.error(e);
        }
    }
}
