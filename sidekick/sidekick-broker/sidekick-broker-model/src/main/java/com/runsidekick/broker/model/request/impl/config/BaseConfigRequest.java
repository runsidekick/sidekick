package com.runsidekick.broker.model.request.impl.config;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public abstract class BaseConfigRequest extends BaseApplicationAwareRequest {

    protected List<ApplicationFilter> applicationFilters;

}
