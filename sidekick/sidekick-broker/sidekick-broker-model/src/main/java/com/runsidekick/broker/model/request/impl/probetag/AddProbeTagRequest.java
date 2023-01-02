package com.runsidekick.broker.model.request.impl.probetag;

import com.runsidekick.broker.model.request.impl.BaseClientRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class AddProbeTagRequest extends BaseClientRequest {

    private String tag;
}
