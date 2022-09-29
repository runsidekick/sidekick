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

    @Value("${phonehome.statistics.threadcount:5}")
    private int phoneHomeStatisticsThreadCount;

    public String getPhoneHomeUrl() {
        return phoneHomeUrl;
    }

    public boolean isPhoneHomeEnabled() {
        return phoneHomeEnabled;
    }

    public boolean isPhoneHomeStatisticsEnabled() {
        return phoneHomeStatisticsEnabled;
    }

    public int getPhoneHomeStatisticsThreadCount() {
        return phoneHomeStatisticsThreadCount;
    }
}
