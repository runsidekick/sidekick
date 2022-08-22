package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

/**
 * @author yasin.kalafat
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPoint extends BaseProbe {

    protected String logExpression;
    protected boolean stdoutEnabled;
    protected String logLevel;

}
