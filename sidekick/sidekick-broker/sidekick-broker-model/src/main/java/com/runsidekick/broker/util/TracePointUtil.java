package com.runsidekick.broker.util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author tolgatakir
 */
public final class TracePointUtil {
    private TracePointUtil() {
    }

    public static final long DEFAULT_TRACE_POINT_EXPIRE_TIME_SECONDS = TimeUnit.MINUTES.toSeconds(30);
    public static final long MAX_TRACE_POINT_EXPIRE_TIME_SECONDS = TimeUnit.DAYS.toSeconds(1);
    public static final int DEFAULT_TRACE_POINT_EXPIRE_COUNT = 50;

    public static int getExpireCount(int expireCount) {
        return expireCount < 0 ? DEFAULT_TRACE_POINT_EXPIRE_COUNT : expireCount;
    }

    public static long getExpireSecs(int expireSecs) {
        return expireSecs < 0 ? DEFAULT_TRACE_POINT_EXPIRE_TIME_SECONDS : expireSecs;
    }

    public static long getExpireTimestamp(int expireSecs) {
        Calendar cl = Calendar.getInstance();

        if (expireSecs == -1) {
            cl.add(Calendar.SECOND, (int) DEFAULT_TRACE_POINT_EXPIRE_TIME_SECONDS);
        } else {
            cl.add(Calendar.SECOND, (int) Math.min(expireSecs, MAX_TRACE_POINT_EXPIRE_TIME_SECONDS));
        }

        return cl.getTimeInMillis();
    }
}
