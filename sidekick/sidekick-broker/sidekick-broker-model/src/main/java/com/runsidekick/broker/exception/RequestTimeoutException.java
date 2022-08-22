package com.runsidekick.broker.exception;

import io.thundra.swark.common.exception.NonCriticalException;
import java.util.concurrent.TimeoutException;

/**
 * @author yasin.kalafat
 */
public class RequestTimeoutException extends TimeoutException implements NonCriticalException {
    public RequestTimeoutException(String message) {
        super(message);
    }
}
