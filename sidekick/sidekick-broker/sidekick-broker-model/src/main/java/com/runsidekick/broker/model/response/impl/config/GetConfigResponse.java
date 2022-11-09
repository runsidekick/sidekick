package com.runsidekick.broker.model.response.impl.config;

import com.runsidekick.broker.model.response.impl.SingleApplicationAwareResponse;
import lombok.Data;

import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Data
public class GetConfigResponse extends SingleApplicationAwareResponse {

    private Map<String, Object> config;

}
