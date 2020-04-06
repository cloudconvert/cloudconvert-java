package com.cloudconvert.test.framework;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class WaitConditionFactoryProvider {

    public static final Duration WAIT_AT_MOST = Duration.of(30000, ChronoUnit.MILLIS);

    /**
     * Make sure that wait polled only once, by making poll interval greater than at most
     */
    public static final Duration WAIT_POLL_DELAY = Duration.of(0, ChronoUnit.MILLIS);
    public static final Duration WAIT_POLL_INTERVAL = WAIT_AT_MOST.plus(WAIT_AT_MOST);

    public ConditionFactory provide(final String id) {
        return Awaitility.with().conditionEvaluationListener(new LoggingConditionEvaluationListener(id)).await()
            .atMost(WAIT_AT_MOST).pollDelay(WAIT_POLL_DELAY).pollInterval(WAIT_POLL_INTERVAL);
    }
}
