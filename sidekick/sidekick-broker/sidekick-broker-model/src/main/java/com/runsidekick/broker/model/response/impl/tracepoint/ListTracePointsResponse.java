package com.runsidekick.broker.model.response.impl.tracepoint;

import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.response.impl.BaseResponse;
import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public class ListTracePointsResponse extends BaseResponse {

    private List<TracePoint> tracePoints;

}
