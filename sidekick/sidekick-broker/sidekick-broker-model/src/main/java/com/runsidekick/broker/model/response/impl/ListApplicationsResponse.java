package com.runsidekick.broker.model.response.impl;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationConfig;
import lombok.Data;

import java.util.List;

/**
 * @author serkan.ozal
 */
@Data
public class ListApplicationsResponse extends BaseResponse {

    private List<Application> applications;
    private List<ApplicationConfig> applicationConfigs;

}
