package com.runsidekick.broker.model.request.impl;

import java.util.List;

/**
 * @author serkan.ozal
 */
public class ListApplicationsRequest extends BaseRequest {

    private List<String> applicationNames;
    private List<String> applicationStages;
    private List<String> applicationVersions;

    public List<String> getApplicationNames() {
        return applicationNames;
    }

    public void setApplicationNames(List<String> applicationNames) {
        this.applicationNames = applicationNames;
    }

    public List<String> getApplicationStages() {
        return applicationStages;
    }

    public void setApplicationStages(List<String> applicationStages) {
        this.applicationStages = applicationStages;
    }

    public List<String> getApplicationVersions() {
        return applicationVersions;
    }

    public void setApplicationVersions(List<String> applicationVersions) {
        this.applicationVersions = applicationVersions;
    }

    @Override
    public String toString() {
        return "ListApplicationsRequest{" +
                "applicationNames=" + applicationNames +
                ", applicationStages=" + applicationStages +
                ", applicationVersions=" + applicationVersions +
                ", id='" + id + '\'' +
                '}';
    }

}
