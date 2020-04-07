package com.cloudconvert.test.framework;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.EvaluatedCondition;
import org.awaitility.core.TimeoutEvent;

@Slf4j
public class LoggingConditionEvaluationListener implements ConditionEvaluationListener<String> {

    private final String id;

    public LoggingConditionEvaluationListener(final String id) {
        this.id = id;
    }

    @Override
    public void conditionEvaluated(final EvaluatedCondition<String> evaluatedCondition) {
        log.info("Evaluated condition for task/job id {}, value {}", id, evaluatedCondition.getValue());
    }

    @Override
    public void onTimeout(final TimeoutEvent timeoutEvent) {
        log.error("Evaluation timed out for task/job id {}", id);
    }
}
