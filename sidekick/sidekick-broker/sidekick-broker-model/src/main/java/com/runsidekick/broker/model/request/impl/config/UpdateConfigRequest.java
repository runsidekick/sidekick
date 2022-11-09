package com.runsidekick.broker.model.request.impl.config;

import lombok.Data;

import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Data
public class UpdateConfigRequest extends BaseConfigRequest {

    private Map<String, Object> config;

}
