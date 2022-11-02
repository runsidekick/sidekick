package com.runsidekick.broker.model.request.impl.probetag;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class EnableProbeTagRequest extends BaseApplicationAwareRequest {

    private String tag;

}
