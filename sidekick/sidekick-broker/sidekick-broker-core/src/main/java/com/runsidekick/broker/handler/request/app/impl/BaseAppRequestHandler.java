package com.runsidekick.broker.handler.request.app.impl;

import com.runsidekick.broker.handler.request.app.AppRequestHandler;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;
import com.runsidekick.broker.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author serkan.ozal
 */
abstract class BaseAppRequestHandler<Req extends Request, Res extends Response>
        implements AppRequestHandler<Req, Res> {

    protected final String requestName;
    protected final Class<Req> requestClass;
    protected final Class<Res> responseClass;

    @Autowired
    protected ApplicationService applicationService;

    BaseAppRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        this.requestName = requestName;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    @Override
    public String getRequestName() {
        return requestName;
    }

    @Override
    public Class<Req> getRequestClass() {
        return requestClass;
    }

    @Override
    public Class<Res> getResponseClass() {
        return responseClass;
    }

}
