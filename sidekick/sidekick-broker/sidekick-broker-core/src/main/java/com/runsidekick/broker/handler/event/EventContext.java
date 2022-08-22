package com.runsidekick.broker.handler.event;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tolgatakir
 */
@Getter
@Setter
public class EventContext {
    private boolean eventUpdated;
    private String rawMessage;
    private boolean broadcast;
    private EventContextExtension extension;
}
