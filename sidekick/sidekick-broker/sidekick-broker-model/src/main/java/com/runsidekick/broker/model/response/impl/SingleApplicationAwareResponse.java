package com.runsidekick.broker.model.response.impl;

import lombok.Data;

/**
 * @author ozge.lule
 */
@Data
public abstract class SingleApplicationAwareResponse extends BaseResponse {

    protected String client;
    protected String applicationInstanceId;

}
