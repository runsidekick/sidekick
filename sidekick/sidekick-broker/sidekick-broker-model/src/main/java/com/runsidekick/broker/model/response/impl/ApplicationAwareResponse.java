package com.runsidekick.broker.model.response.impl;

import lombok.Data;

import java.util.List;

/**
 * @author ozge.lule
 */
@Data
public abstract class ApplicationAwareResponse extends BaseResponse {

    protected String client;
    protected List<String> applicationInstanceIds;

}
