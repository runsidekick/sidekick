package com.runsidekick.broker.model.response.impl.tracepoint;

import lombok.Data;

/**
 * @author ozge.lule
 */
@Data
public class RemoveBatchTracePointResponse extends ApplicationAwareTracePointResponse {

    private Long deletedTracePointCount;
    private Long undeletedTracePointCount;

}
