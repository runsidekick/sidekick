package com.runsidekick.broker.model.request.impl.probetag;

import com.runsidekick.broker.model.request.impl.BaseClientRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class RemoveProbeTagRequest extends BaseClientRequest {

    private String tag;
}
