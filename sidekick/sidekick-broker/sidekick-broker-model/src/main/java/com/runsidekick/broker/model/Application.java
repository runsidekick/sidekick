package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author serkan.ozal
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {

    private String workspaceId;
    private String instanceId;
    private String name;
    private String stage;
    private String version;
    private String ip;
    private String hostName;
    private String runtime;
    private List<TracePoint> tracePoints;
    private List<LogPoint> logPoints;
    private List<CustomTag> customTags;

    public Application() {
        this.logPoints = new ArrayList<>();
        this.tracePoints = new ArrayList<>();
        this.customTags = new ArrayList<>();
    }

    public void addTracePoint(TracePoint tracePoint) {
        if (tracePoints == null) {
            tracePoints = new ArrayList<>();
        }
        tracePoints.add(tracePoint);
    }

    public void removeTracePoint(TracePoint tracePoint) {
        if (tracePoints != null) {
            tracePoints.remove(tracePoint);
        }
    }

    public void addLogPoint(LogPoint logPoint) {
        if (logPoints == null) {
            logPoints = new ArrayList<>();
        }
        logPoints.add(logPoint);
    }

    public void removeTracePoint(LogPoint logPoint) {
        if (logPoints != null) {
            logPoints.remove(logPoint);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Application that = (Application) o;
        return Objects.equals(instanceId, that.instanceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(stage, that.stage) &&
                Objects.equals(version, that.version) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(hostName, that.hostName) &&
                Objects.equals(runtime, that.runtime) &&
                Objects.equals(tracePoints, that.tracePoints) &&
                Objects.equals(logPoints, that.logPoints) &&
                Objects.equals(customTags, that.customTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, name, stage, version, ip, hostName, runtime, tracePoints, logPoints);
    }

    @Override
    public String toString() {
        return "Application{" +
                "instanceId='" + instanceId + '\'' +
                ", name='" + name + '\'' +
                ", stage='" + stage + '\'' +
                ", version='" + version + '\'' +
                ", ip='" + ip + '\'' +
                ", hostName='" + hostName + '\'' +
                ", runtime='" + runtime + '\'' +
                ", tracePoints=" + tracePoints +
                ", logPoints=" + logPoints +
                ", customTags=" + customTags +
                '}';
    }

    public static class CustomTag {

        private String tagName;
        private String tagValue;

        public CustomTag() {
        }

        public CustomTag(String tagName, String tagValue) {
            this.tagName = tagName;
            this.tagValue = tagValue;
        }

        public String getTagName() {
            return tagName;
        }

        public String getTagValue() {
            return tagValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CustomTag that = (CustomTag) o;
            return Objects.equals(tagName, that.tagName) && Objects.equals(tagValue, that.tagValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tagName, tagValue);
        }

        @Override
        public String toString() {
            return "CustomTag{" +
                    "tagName='" + tagName + '\'' +
                    ", tagValue='" + tagValue + '\'' +
                    '}';
        }

    }

}
