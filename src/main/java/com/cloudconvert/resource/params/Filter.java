package com.cloudconvert.resource.params;

import lombok.Getter;

public enum Filter {

    JOB_ID("job_id"),
    STATUS("status"),
    OPERATION("operation"),
    INPUT_FORMAT("input_format"),
    OUTPUT_FORMAT("output_format"),
    ENGINE("engine"),
    ENGINE_VERSION("engine_version"),
    TAG("tag"),
    URL("url");

    @Getter
    private final String label;

    Filter(final String label) {
        this.label = label;
    }
}