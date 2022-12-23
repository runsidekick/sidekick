package com.runsidekick.broker.model.response.impl.probetag;

import com.runsidekick.broker.model.response.impl.BaseResponse;
import com.runsidekick.model.ProbeTag;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class ListProbeTagsResponse extends BaseResponse {

    private List<ProbeTag> probeTags;
}
