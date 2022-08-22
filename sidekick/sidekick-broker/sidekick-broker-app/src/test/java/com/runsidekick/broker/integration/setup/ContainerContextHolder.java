package com.runsidekick.broker.integration.setup;

import io.thundra.swark.env.utils.MySQLContainerContext;
import io.thundra.swark.env.utils.RedisContainerContext;

/**
 * @author serkan.ozal
 */
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
