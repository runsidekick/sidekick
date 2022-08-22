package com.runsidekick.broker.repository.impl;

import com.runsidekick.broker.repository.LogPointExpireCountRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.BatchOptions;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RAtomicLongAsync;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.runsidekick.broker.util.LogPointUtil.DEFAULT_LOG_POINT_EXPIRE_COUNT;
import static com.runsidekick.broker.util.LogPointUtil.getExpireTimestamp;


/**
 * @author yasin.kalafat
 */
@Repository
public class LogPointExpireCountRepositoryImpl implements LogPointExpireCountRepository {
    private final Logger logger = LogManager.getLogger(LogPointExpireCountRepositoryImpl.class);

    private final RedissonClient client;

    public LogPointExpireCountRepositoryImpl(RedissonClient client) {
        this.client = client;
    }

    @Override
    public void putLogPointExpireCount(String workspaceId, String logPointId, Integer expireCount, Integer expireSecs) {
        if (expireCount == -1) {
            expireCount = DEFAULT_LOG_POINT_EXPIRE_COUNT;
        }
        RBatch batch = client.createBatch(BatchOptions.defaults());
        RAtomicLongAsync value = batch.getAtomicLong(getKey(buildResourceKey(workspaceId, logPointId)));
        value.setAsync(expireCount);
        value.expireAtAsync(getExpireTimestamp(expireSecs));
        batch.execute();
    }

    @Override
    public boolean removeLogPointExpireCount(String workspaceId, String logPointId) {
        RAtomicLong value = client.getAtomicLong(getKey(buildResourceKey(workspaceId, logPointId)));
        return value.delete();
    }

    @Override
    public boolean removeLogPointsExpireCount(String workspaceId, List<String> logPointIds) {
        RBatch batch = client.createBatch();
        for (String logPointId : logPointIds) {
            batch.getAtomicLong(getKey(buildResourceKey(workspaceId, logPointId))).deleteAsync();
        }

        batch.execute();
        return true;
    }

    @Override
    public CompletableFuture<Boolean> checkExpireAndDecrementLogPointExpireCount(
            String workspaceId, String logPointId) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        RAtomicLong value = client.getAtomicLong(getKey(buildResourceKey(workspaceId, logPointId)));
        RFuture<Boolean> isExistFuture = value.isExistsAsync();
        isExistFuture.onComplete((isExist, exception) -> {
            if (exception != null) {
                logger.error("Error occurred check key is exist in redis.", exception);
                completableFuture.complete(false);
                return;
            }
            if (isExist) {
                RFuture<Long> future = value.decrementAndGetAsync();
                future.onComplete((expireCount, e) -> {
                    if (e != null) {
                        logger.error("Error occurred decrement and get key in redis.", e);
                        completableFuture.complete(false);
                        return;
                    }
                    completableFuture.complete(expireCount.equals(0L));
                });
            } else {
                completableFuture.complete(false);
            }
        });
        return completableFuture;
    }

    private String buildResourceKey(String workspaceId, String logPointId) {
        return workspaceId + "_" + logPointId;
    }

    private String getKey(String resourceKey) {
        return "LogPoint::ExpireCount:" + resourceKey;
    }
}
