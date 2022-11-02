package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPointConfig extends LogPoint implements ProbeConfig {

    private List<ApplicationFilter> applicationFilters;

    @Override
    public String toString() {
        return "LogPointConfig{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNo=" + lineNo +
                ", client='" + client + '\'' +
                ", conditionExpression='" + conditionExpression + '\'' +
                ", expireSecs=" + expireSecs +
                ", expireCount=" + expireCount +
                ", fileHash='" + fileHash + '\'' +
                ", disabled=" + disabled +
                ", webhookIds=" + webhookIds +
                ", logExpression='" + logExpression + '\'' +
                ", stdoutEnabled=" + stdoutEnabled +
                ", logLevel='" + logLevel + '\'' +
                ", applicationFilters=" + applicationFilters +
                ", probeName='" + probeName + '\'' +
                '}';
    }

}
