package com.runsidekick.broker.model.request.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.runsidekick.broker.model.request.Request;

/**
 * @author serkan.ozal
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseRequest implements Request {

    protected String id;
    protected String client;

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

}
