package com.runsidekick.broker.model.request.impl.refereceevent;

import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.request.impl.BaseClientRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class GetReferenceEventRequest extends BaseClientRequest {

    private String probeId;
    private ProbeType probeType;

}
