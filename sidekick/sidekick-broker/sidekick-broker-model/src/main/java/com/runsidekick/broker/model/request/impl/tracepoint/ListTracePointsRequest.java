package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.request.impl.BaseRequest;
import lombok.Data;

/**
 * @author ozge.lule
 */
@Data
public class ListTracePointsRequest extends BaseRequest {

    @Override
    public String toString() {
        return "ListTracePointsRequest{" +
                "id='" + id + '\'' +
                '}';
    }

}
