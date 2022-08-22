package com.runsidekick.broker.model.response.impl.tracepoint;

import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.response.impl.SingleApplicationAwareResponse;
import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public class FilterTracePointsResponse extends SingleApplicationAwareResponse {

    private List<TracePoint> tracePoints;
    protected String applicationName;

}
