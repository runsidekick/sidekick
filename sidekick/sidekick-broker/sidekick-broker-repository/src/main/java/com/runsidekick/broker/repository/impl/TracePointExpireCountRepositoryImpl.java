package com.runsidekick.broker.repository.impl;

import com.runsidekick.broker.repository.TracePointExpireCountRepository;
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

import static com.runsidekick.broker.util.TracePointUtil.DEFAULT_TRACE_POINT_EXPIRE_COUNT;
import static com.runsidekick.broker.util.TracePointUtil.getExpireTimestamp;

/**
 * @author tolgatakir
 */
@Repository
public class TracePointExpireCountRepositoryImpl implements TracePointExpireCountRepository {
    private final Logger logger = LogManager.getLogger(TracePointExpireCountRepositoryImpl.class);

    private final RedissonClient client;

    public TracePointExpireCountRepositoryImpl(RedissonClient client) {
        this.client = client;
    }

    @Override
    public void putTracePointExpireCount(
            String workspaceId, String tracePointId, Integer expireCount, Integer expireSecs) {
        if (expireCount == -1) {
            expireCount = DEFAULT_TRACE_POINT_EXPIRE_COUNT;
        }
        RBatch batch = client.createBatch(BatchOptions.defaults());
        RAtomicLongAsync value = batch.getAtomicLong(getKey(buildResourceKey(workspaceId, tracePointId)));
        value.setAsync(expireCount);
        value.expireAtAsync(getExpireTimestamp(expireSecs));
        batch.execute();
    }

    @Override
    public boolean removeTracePointExpireCount(String workspaceId, String tracePointId) {
        RAtomicLong value = client.getAtomicLong(getKey(buildResourceKey(workspaceId, tracePointId)));
        return value.delete();
    }

    @Override
    public boolean removeTracePointsExpireCount(String workspaceId, List<String> tracePointIds) {
        RBatch batch = client.createBatch();
        for (String tracePointId : tracePointIds) {
            batch.getAtomicLong(getKey(buildResourceKey(workspaceId, tracePointId))).deleteAsync();
        }

        batch.execute();
        return true;
    }

    @Override
    public CompletableFuture<Boolean> checkExpireAndDecrementTracePointExpireCount(
            String workspaceId, String tracePointId) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        RAtomicLong value = client.getAtomicLong(getKey(buildResourceKey(workspaceId, tracePointId)));
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

    private String buildResourceKey(String workspaceId, String tracePointId) {
        return workspaceId + "_" + tracePointId;
    }

    private String getKey(String resourceKey) {
        return "TracePoint::ExpireCount:" + resourceKey;
    }
}
