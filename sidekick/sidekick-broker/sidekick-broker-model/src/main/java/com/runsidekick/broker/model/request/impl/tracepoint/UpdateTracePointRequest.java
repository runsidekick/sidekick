package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public class UpdateTracePointRequest extends BaseApplicationAwareRequest {

    private String tracePointId;
    private String fileName;
    private int lineNo;
    private String fileHash;
    private String conditionExpression;
    private int expireSecs;
    private int expireCount;
    private boolean enableTracing;
    private boolean disable;
    private List<String> webhookIds;
    private String probeName;
    private List<String> tags;

}
