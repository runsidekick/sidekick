package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TracePointConfig extends TracePoint implements ProbeConfig {

    private List<ApplicationFilter> applicationFilters;

    @Override
    public String toString() {
        return "TracePointConfig{" +
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
                ", tracingEnabled=" + tracingEnabled +
                ", applicationFilters=" + applicationFilters +
                ", predefined=" + predefined +
                ", probeName='" + probeName + '\'' +
                '}';
    }
}
