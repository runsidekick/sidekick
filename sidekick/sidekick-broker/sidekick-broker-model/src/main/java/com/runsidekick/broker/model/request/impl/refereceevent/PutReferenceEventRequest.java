package com.runsidekick.broker.model.request.impl.refereceevent;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.request.impl.BaseClientRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class PutReferenceEventRequest extends BaseClientRequest {

    private String probeId;
    private ProbeType probeType;
    private String event;
    private ApplicationFilter applicationFilter;

}
