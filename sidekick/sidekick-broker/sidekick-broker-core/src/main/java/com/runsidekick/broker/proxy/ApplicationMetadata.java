package com.runsidekick.broker.proxy;

import com.runsidekick.broker.model.Application;

import java.util.List;

/**
 * @author serkan.ozal
 */
public class ApplicationMetadata implements ChannelMetadata {

    final String instanceId;
    final String name;
    final String stage;
    final String version;
    final String ip;
    final String hostName;
    final String runtime;
    final List<Application.CustomTag> customTags;

    public ApplicationMetadata(String instanceId,
                               String name,
                               String stage,
                               String version,
                               String ip,
                               String hostName,
                               String runtime,
                               List<Application.CustomTag> appCustomTags) {
        this.instanceId = instanceId;
        this.name = name;
        this.stage = stage;
        this.version = version;
        this.ip = ip;
        this.hostName = hostName;
        this.runtime = runtime;
        this.customTags = appCustomTags;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getName() {
        return name;
    }

    public String getStage() {
        return stage;
    }

    public String getVersion() {
        return version;
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    public String getRuntime() {
        return runtime;
    }

    public List<Application.CustomTag> getCustomTags() {
        return customTags;
    }
}
