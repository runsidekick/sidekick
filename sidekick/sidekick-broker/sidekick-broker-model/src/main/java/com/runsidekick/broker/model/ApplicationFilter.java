package com.runsidekick.broker.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

/**
 * @author ozge.lule
 */
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationFilter {

    private String name;
    private String version;
    private String stage;
    private Map<String, String> customTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Map<String, String> getCustomTags() {
        return customTags;
    }

    public void setCustomTags(Map<String, String> customTags) {
        this.customTags = customTags;
    }

    @Override
    public String toString() {
        return "ApplicationFilter{" +
                "name='" + name + '\'' +
                ", stage='" + stage + '\'' +
                ", version='" + version + '\'' +
                ", customTags='" + customTags + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationFilter that = (ApplicationFilter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(stage, that.stage) &&
                Objects.equals(version, that.version) &&
                Objects.equals(customTags, that.customTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stage, version, customTags);
    }

}
