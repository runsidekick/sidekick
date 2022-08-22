package com.runsidekick.broker.exception;

import io.thundra.swark.common.exception.NonCriticalException;

/**
 * @author yasin.kalafat
 */
public class WSClientNotConnectedException extends Exception implements NonCriticalException {
    public WSClientNotConnectedException(String message) {
        super(message);
    }
}
