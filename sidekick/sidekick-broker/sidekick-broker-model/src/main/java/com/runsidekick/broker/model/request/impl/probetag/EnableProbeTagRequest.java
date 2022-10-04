package com.runsidekick.broker.model.request.impl.probetag;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class EnableProbeTagRequest extends BaseApplicationAwareRequest {

    private List<ApplicationFilter> applicationFilters;
    private String tag;

}
