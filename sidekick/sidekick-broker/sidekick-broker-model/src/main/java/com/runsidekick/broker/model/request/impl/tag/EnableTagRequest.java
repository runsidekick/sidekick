package com.runsidekick.broker.model.request.impl.tag;

import com.runsidekick.broker.model.request.impl.BaseApplicationAwareRequest;
import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class EnableTagRequest extends BaseApplicationAwareRequest {

    private String tag;

}
