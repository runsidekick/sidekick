package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

/**
 * @author ozge.lule
 */
@Data
public class DisableTracePointRequest extends BaseApplicationAwareRequest {

    private String tracePointId;

    @Override
    public String toString() {
        return "DisableTracePointRequest{" +
                "tracePointId='" + tracePointId + '\'' +
                ", applications=" + applications +
                ", persist=" + persist +
                ", client='" + client + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

}
