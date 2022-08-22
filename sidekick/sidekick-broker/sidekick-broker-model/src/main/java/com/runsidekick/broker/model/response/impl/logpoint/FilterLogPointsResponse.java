package com.runsidekick.broker.model.response.impl.logpoint;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.response.impl.SingleApplicationAwareResponse;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class FilterLogPointsResponse extends SingleApplicationAwareResponse {

    private List<LogPoint> logPoints;
    protected String applicationName;

}
