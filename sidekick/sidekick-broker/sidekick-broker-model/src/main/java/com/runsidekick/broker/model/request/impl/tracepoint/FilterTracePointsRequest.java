package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.BaseRequest;
import lombok.Data;

import java.util.Map;

/**
 * @author ozge.lule
 */
@Data
public class FilterTracePointsRequest extends BaseRequest {
    private ApplicationFilter applicationFilter = new ApplicationFilter();

    private String applicationInstanceId;

    public FilterTracePointsRequest() {

    }

    public FilterTracePointsRequest(String name, String version, String stage, Map<String, String> customTags,
                                    String applicationInstanceId) {
        ApplicationFilter applicationFilter = new ApplicationFilter();
        applicationFilter.setName(name);
        applicationFilter.setVersion(version);
        applicationFilter.setStage(stage);
        applicationFilter.setCustomTags(customTags);

        this.applicationFilter = applicationFilter;
        this.applicationInstanceId = applicationInstanceId;
    }

    @Override
    public String toString() {
        return "FilterTracePointsRequest{" +
                "ApplicationFilter{" +
                "name='" + applicationFilter.getName() + '\'' +
                ", stage='" + applicationFilter.getStage() + '\'' +
                ", version='" + applicationFilter.getVersion() + '\'' +
                ", customTags='" + applicationFilter.getCustomTags() + '\'' +
                '}' + '\'' +
                ", applicationInstanceId= '" + applicationInstanceId + '\'' +
                '}';
    }
}
