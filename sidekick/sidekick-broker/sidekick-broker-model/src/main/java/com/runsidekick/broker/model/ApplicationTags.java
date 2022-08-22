package com.runsidekick.broker.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author serkan.ozal
 */
public class ApplicationTags {

    private Set<String> applicationNames;
    private Set<String> applicationVersions;
    private Set<String> applicationStages;
    private Map<String, Set<String>> applicationCustomTags;

    public ApplicationTags() {
        this.applicationCustomTags = new HashMap<>();
    }

    public Set<String> getApplicationNames() {
        return applicationNames;
    }

    public void setApplicationNames(Set<String> applicationNames) {
        this.applicationNames = applicationNames;
    }

    public Set<String> getApplicationVersions() {
        return applicationVersions;
    }

    public void setApplicationVersions(Set<String> applicationVersions) {
        this.applicationVersions = applicationVersions;
    }

    public Set<String> getApplicationStages() {
        return applicationStages;
    }

    public void setApplicationStages(Set<String> applicationStages) {
        this.applicationStages = applicationStages;
    }

    public Map<String, Set<String>> getApplicationCustomTags() {
        return applicationCustomTags;
    }

    public void setApplicationCustomTags(Map<String, Set<String>> applicationCustomTags) {
        this.applicationCustomTags = applicationCustomTags;
    }

    public void addApplicationCustomTag(String tagName, String tagValue) {
        if (applicationCustomTags.containsKey(tagName)) {
            applicationCustomTags.get(tagName).add(tagValue);
        } else {
            Set<String> tagValues = new HashSet<>();
            tagValues.add(tagValue);
            applicationCustomTags.put(tagName, tagValues);
        }
    }

    public void removeApplicationCustomTag(String tagName, String tagValue) {
        if (applicationCustomTags.containsKey(tagName)) {
            Set<String> values = applicationCustomTags.get(tagName);
            values.remove(tagValue);
        }
    }

    @Override
    public String toString() {
        return "ApplicationTags{" +
                "applicationNames=" + applicationNames +
                ", applicationCustomTags=" + applicationCustomTags +
                ", applicationVersions=" + applicationVersions +
                ", applicationStages=" + applicationStages +
                '}';
    }

}
