package com.runsidekick.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author yasin.kalafat
 */
@Configuration
public class PhoneHomeConfig {

    @Value("${phonehome.url:}")
    private String phoneHomeUrl;

    @Value("${phonehome.enabled:true}")
    private boolean phoneHomeEnabled;

    @Value("${phonehome.statistics.enabled:true}")
    private boolean phoneHomeStatisticsEnabled;

    public String getPhoneHomeUrl() {
        return phoneHomeUrl;
    }

    public boolean isPhoneHomeEnabled() {
        return phoneHomeEnabled;
    }

    public boolean isPhoneHomeStatisticsEnabled() {
        return phoneHomeStatisticsEnabled;
    }
}
