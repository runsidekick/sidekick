package com.runsidekick.broker.model.response.impl.logpoint;

import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.response.impl.ApplicationAwareProbeResponse;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public abstract class ApplicationAwareLogPointResponse extends ApplicationAwareProbeResponse<LogPointConfig> {

}
