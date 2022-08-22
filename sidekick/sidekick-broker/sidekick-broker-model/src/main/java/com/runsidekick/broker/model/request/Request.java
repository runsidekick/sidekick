package com.runsidekick.broker.model.request;

/**
 * @author serkan.ozal
 */
public interface Request {

    default String getType() {
        return "Request";
    }

    String getName();

    String getId();

    String getClient();
}
