package com.runsidekick.broker.model.response.impl;

import com.runsidekick.broker.model.ProbeConfig;
import lombok.Data;


/**
 * @author yasin.kalafat
 */
@Data
public abstract class ApplicationAwareProbeResponse<T extends ProbeConfig> extends ApplicationAwareResponse {

    private T probeConfig;

}
