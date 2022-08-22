package com.runsidekick.broker.integration.setup;

import java.util.Map;

/**
 * @author serkan.ozal
 */
public class AppCredentials {

    private final String apiKey;
    private final String appInstanceId;
    private final String appName;
    private final String appStage;
    private final String appVersion;
    private final Map<String, String> appCustomTags;

    public AppCredentials(String apiKey, String appInstanceId) {
        this.apiKey = apiKey;
        this.appInstanceId = appInstanceId;
        this.appName = null;
        this.appStage = null;
        this.appVersion = null;
        this.appCustomTags = null;
    }

    public AppCredentials(String apiKey, String appInstanceId,
                          String appName, String appStage, String appVersion) {
        this.apiKey = apiKey;
        this.appInstanceId = appInstanceId;
        this.appName = appName;
        this.appStage = appStage;
        this.appVersion = appVersion;
        this.appCustomTags = null;
    }

    public AppCredentials(String apiKey, String appInstanceId,
                          String appName, String appStage, String appVersion, Map<String, String> appCustomTags) {
        this.apiKey = apiKey;
        this.appInstanceId = appInstanceId;
        this.appName = appName;
        this.appStage = appStage;
        this.appVersion = appVersion;
        this.appCustomTags = appCustomTags;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppStage() {
        return appStage;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public Map<String, String> getAppCustomTags() {
        return appCustomTags;
    }
}
