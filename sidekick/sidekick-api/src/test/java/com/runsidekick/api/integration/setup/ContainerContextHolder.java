package com.runsidekick.api.integration.setup;

import io.thundra.swark.env.utils.MySQLContainerContext;
import io.thundra.swark.env.utils.RedisContainerContext;
import org.springframework.stereotype.Component;

@Component
public class ContainerContextHolder {

    volatile MySQLContainerContext mySQLContainerContext;
    volatile RedisContainerContext redisContainerContext;

    public MySQLContainerContext getMySQLContainerContext() {
        return mySQLContainerContext;
    }

    public RedisContainerContext getRedisContainerContext() {
        return redisContainerContext;
    }

}
