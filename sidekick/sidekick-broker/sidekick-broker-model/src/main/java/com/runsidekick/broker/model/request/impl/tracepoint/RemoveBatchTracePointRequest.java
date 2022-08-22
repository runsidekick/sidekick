package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public class RemoveBatchTracePointRequest extends BaseApplicationAwareRequest {

    private List<String> tracePointIds;

    @Override
    public String toString() {
        return "RemoveTracePointRequest{" +
                "tracePointIds='" + tracePointIds + '\'' +
                ", applications=" + applications +
                ", persist=" + persist +
                ", client='" + client + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

}
