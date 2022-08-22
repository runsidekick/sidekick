package com.runsidekick.broker.model.request.impl.tracepoint;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public class PutTracePointRequest extends BaseApplicationAwareRequest {

    private String fileName;
    private int lineNo;
    private String conditionExpression;
    private int expireSecs;
    private int expireCount;
    private boolean enableTracing;
    private String fileHash;
    private boolean disable;
    private List<ApplicationFilter> applicationFilters;
    private List<String> webhookIds;
    private boolean predefined;
    private String probeName;

    public static PutTracePointRequest of(TracePoint tracePoint, ApplicationFilter applicationFilter,
                                          Application application) {
        PutTracePointRequest request = new PutTracePointRequest();
        request.setEnableTracing(true);
        request.setApplicationFilters(Collections.singletonList(applicationFilter));
        request.setApplications(Collections.singletonList(application.getInstanceId()));
        request.setExpireCount(tracePoint.getExpireCount());
        request.setExpireSecs(tracePoint.getExpireSecs());
        request.setConditionExpression(tracePoint.getConditionExpression());
        request.setFileHash(tracePoint.getFileHash());
        request.setFileName(tracePoint.getFileName());
        request.setLineNo(tracePoint.getLineNo());
        request.setDisable(tracePoint.isDisabled());
        request.setPersist(true);
        request.setClient(tracePoint.getClient());
        request.setId(tracePoint.getId());
        request.setWebhookIds(tracePoint.getWebhookIds());
        request.setPredefined(tracePoint.isPredefined());
        request.setProbeName(tracePoint.getProbeName());

        return request;
    }

}
