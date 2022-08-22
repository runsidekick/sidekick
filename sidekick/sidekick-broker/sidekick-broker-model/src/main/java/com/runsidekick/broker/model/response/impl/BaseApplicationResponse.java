package com.runsidekick.broker.model.response.impl;

import lombok.Data;

/**
 * @author serkan.ozal
 */
@Data
public abstract class BaseApplicationResponse extends BaseResponse {

    protected String client;
    protected String applicationName;
    protected String applicationInstanceId;

}
