package com.runsidekick.broker.model.response.impl;

import com.runsidekick.broker.error.CodedError;
import com.runsidekick.broker.model.response.Response;
import lombok.Data;

/**
 * @author serkan.ozal
 */
@Data
public abstract class BaseResponse implements Response {

    protected String requestId;
    protected boolean erroneous;
    protected int errorCode;
    protected String errorMessage;

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public void setError(CodedError codedError, Object... args) {
        this.erroneous = true;
        this.errorCode = codedError.getCode();
        this.errorMessage = codedError.formatMessage(args);
    }
}
