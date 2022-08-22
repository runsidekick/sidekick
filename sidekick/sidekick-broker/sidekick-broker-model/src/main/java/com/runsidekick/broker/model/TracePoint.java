package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

/**
 * @author serkan.ozal
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TracePoint extends BaseProbe {

    protected boolean tracingEnabled;

}
