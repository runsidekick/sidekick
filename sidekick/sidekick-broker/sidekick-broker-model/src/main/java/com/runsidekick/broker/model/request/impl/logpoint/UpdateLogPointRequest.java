package com.runsidekick.broker.model.request.impl.logpoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class UpdateLogPointRequest extends BaseApplicationAwareRequest {

    private String logPointId;
    private String fileName;
    private int lineNo;
    private String fileHash;
    private String conditionExpression;
    private int expireSecs;
    private int expireCount;
    private boolean disable;
    private String logExpression;
    private boolean stdoutEnabled;
    private String logLevel;
    private List<String> webhookIds;
    private String probeName;
    private List<String> tags;
}
