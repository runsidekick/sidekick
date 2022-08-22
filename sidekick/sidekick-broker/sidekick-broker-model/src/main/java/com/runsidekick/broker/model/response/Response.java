package com.runsidekick.broker.model.response;

/**
 * @author serkan.ozal
 */
public interface Response {

    default String getType() {
        return "Response";
    }

    default String getSource() {
        return "Broker";
    }

    String getName();

    String getRequestId();

    boolean isErroneous();

    int getErrorCode();

    String getErrorMessage();

}
