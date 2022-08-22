package com.runsidekick.broker.model.response.impl;

import com.runsidekick.broker.error.ErrorCodes;
import lombok.Data;

/**
 * @author serkan.ozal
 */
@Data
public class TargetApplicationNotAvailableResponse extends BaseApplicationResponse {

    public TargetApplicationNotAvailableResponse() {
    }

    public TargetApplicationNotAvailableResponse(String requestId, String appInstanceId) {
        setRequestId(requestId);
        setApplicationInstanceId(appInstanceId);
        setError(ErrorCodes.TARGET_APPLICATION_NOT_AVAILABLE, appInstanceId);
    }

}
