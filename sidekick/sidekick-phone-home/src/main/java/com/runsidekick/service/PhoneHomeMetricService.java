package com.runsidekick.service;


/**
 * @author yasin.kalafat
 */
public interface PhoneHomeMetricService {

    void sendServerUpEvent(long startTime);

    void sendServerDownEvent(long startTime, long finishTime);
}
