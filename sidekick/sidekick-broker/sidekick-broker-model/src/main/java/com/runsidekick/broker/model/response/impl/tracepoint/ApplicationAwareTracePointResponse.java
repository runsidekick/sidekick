package com.runsidekick.broker.model.response.impl.tracepoint;

import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.response.impl.ApplicationAwareProbeResponse;
import lombok.Data;

@Data
public abstract class ApplicationAwareTracePointResponse extends ApplicationAwareProbeResponse<TracePointConfig> {

}
