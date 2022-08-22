package com.runsidekick.broker.model.request.impl.logpoint;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class RemoveBatchLogPointRequest extends BaseApplicationAwareRequest {

    private List<String> logPointIds;

}
