package com.runsidekick.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
public class PhoneHomeMetric implements Cloneable {

    private String osName;
    private String osArch;
    private String osVersion;
    private String javaVersion;
    private String hostName;
    private String appVersion;
    private String macAddress;
    private PhoneHomeEventType eventType;
    private Map<String, Object> eventDetails;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
