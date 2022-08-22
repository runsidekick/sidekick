package com.runsidekick.broker.handler.request.client;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;
import com.runsidekick.broker.proxy.ChannelInfo;

/**
 * @author serkan.ozal
 */
public interface ClientRequestHandler<Req extends Request, Res extends Response> {

    String getRequestName();
    Class<Req> getRequestClass();
    Class<Res> getResponseClass();

    Res handleRequest(ChannelInfo channelInfo, Req request, RequestContext requestContext);

}
