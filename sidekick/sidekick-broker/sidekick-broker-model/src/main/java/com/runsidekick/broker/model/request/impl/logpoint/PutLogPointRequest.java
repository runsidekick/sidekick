package com.runsidekick.broker.model.request.impl.logpoint;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class PutLogPointRequest extends BaseApplicationAwareRequest {

    private String fileName;
    private int lineNo;
    private String conditionExpression;
    private int expireSecs;
    private int expireCount;
    private String fileHash;
    private String logExpression;
    private boolean stdoutEnabled;
    private String logLevel;
    private boolean disable;
    private List<ApplicationFilter> applicationFilters;
    private List<String> webhookIds;
    private boolean predefined;
    private String probeName;

    public static PutLogPointRequest of(LogPoint logPoint, ApplicationFilter applicationFilter,
                                        Application application) {
        PutLogPointRequest request = new PutLogPointRequest();
        request.setApplicationFilters(Collections.singletonList(applicationFilter));
        request.setApplications(Collections.singletonList(application.getInstanceId()));
        request.setExpireCount(logPoint.getExpireCount());
        request.setExpireSecs(logPoint.getExpireSecs());
        request.setConditionExpression(logPoint.getConditionExpression());
        request.setFileHash(logPoint.getFileHash());
        request.setFileName(logPoint.getFileName());
        request.setLineNo(logPoint.getLineNo());
        request.setDisable(logPoint.isDisabled());
        request.setPersist(true);
        request.setClient(logPoint.getClient());
        request.setId(logPoint.getId());
        request.setLogExpression(logPoint.getLogExpression());
        request.setStdoutEnabled(logPoint.isStdoutEnabled());
        request.setLogLevel(logPoint.getLogLevel());
        request.setWebhookIds(logPoint.getWebhookIds());
        request.setPredefined(logPoint.isPredefined());
        request.setProbeName(logPoint.getProbeName());

        return request;
    }
}
