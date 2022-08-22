package com.runsidekick.broker.model.event.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tolgatakir
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TracePointSnapshotEvent extends BaseEvent {

    private String tracePointId;
    private String fileName;
    private String className;
    private int lineNo;
    private String methodName;
    private String traceId;
    private String transactionId;
    private String spanId;

}
