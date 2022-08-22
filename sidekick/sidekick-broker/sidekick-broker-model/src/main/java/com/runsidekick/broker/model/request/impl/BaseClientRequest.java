package com.runsidekick.broker.model.request.impl;

import com.runsidekick.broker.model.request.ClientRequest;
import lombok.Data;

/**
 * @author serkan.ozal
 */
@Data
public abstract class BaseClientRequest
        extends BaseRequest
        implements ClientRequest {

    protected String client;

}
