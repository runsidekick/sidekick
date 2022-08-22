package com.runsidekick.broker.model.response.impl.logpoint;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.response.impl.BaseResponse;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class ListLogPointsResponse extends BaseResponse {

    private List<LogPoint> logPoints;

}
