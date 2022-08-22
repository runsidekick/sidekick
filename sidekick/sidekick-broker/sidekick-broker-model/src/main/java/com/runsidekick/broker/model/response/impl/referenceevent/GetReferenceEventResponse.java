package com.runsidekick.broker.model.response.impl.referenceevent;

import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.model.response.impl.BaseResponse;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class GetReferenceEventResponse extends BaseResponse {

    private ReferenceEvent referenceEvent;

}
