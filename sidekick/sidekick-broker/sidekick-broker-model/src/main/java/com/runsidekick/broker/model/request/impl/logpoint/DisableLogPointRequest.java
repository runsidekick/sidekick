package com.runsidekick.broker.model.request.impl.logpoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class DisableLogPointRequest extends BaseApplicationAwareRequest {

    private String logPointId;

}
