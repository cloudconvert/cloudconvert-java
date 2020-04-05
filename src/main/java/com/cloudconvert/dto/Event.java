package com.cloudconvert.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum Event {

    /**
     * Emitted when a new job was just created.
     */
    JOB_CREATED("job.created"),

    /**
     * A job (and all associated tasks) completed successfully. The payload includes the job as shown by the example payload on the right.
     */
    JOB_FINISHED("job.finished"),

    /**
     * A job failed.
     */
    JOB_FAILED("job.failed");

    @Getter
    @JsonValue
    private final String label;

    Event(final String label) {
        this.label = label;
    }
}