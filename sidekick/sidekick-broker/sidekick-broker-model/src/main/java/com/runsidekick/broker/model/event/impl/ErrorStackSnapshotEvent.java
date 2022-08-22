package com.runsidekick.broker.model.event.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yasin.kalafat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorStackSnapshotEvent extends BaseEvent {

    private String errorStackId;
    private String fileName;
    private String className;
    private int lineNo;
    private String methodName;
    private String traceId;
    private String transactionId;
    private String spanId;

}
