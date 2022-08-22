package com.runsidekick.broker.model;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author yasin.kalafat
 */
@Data
public abstract class BaseProbe {
    protected String id;
    protected String fileName;
    protected int lineNo;
    protected String client;
    protected String conditionExpression;
    protected int expireSecs;
    protected int expireCount;
    protected String fileHash;
    protected boolean disabled;
    protected List<String> webhookIds;
    protected boolean predefined;
    protected String probeName; // for predefined probe

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogPoint that = (LogPoint) o;
        return lineNo == that.lineNo &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, lineNo, client);
    }
}
