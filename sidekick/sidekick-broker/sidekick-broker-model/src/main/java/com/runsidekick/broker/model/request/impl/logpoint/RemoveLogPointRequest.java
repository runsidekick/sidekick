package com.runsidekick.broker.model.request.impl.logpoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class RemoveLogPointRequest extends BaseApplicationAwareRequest {

    private String logPointId;

}
