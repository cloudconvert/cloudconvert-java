package com.cloudconvert.resource.params;

import lombok.Getter;

public enum Include {

    RETRIES("retries"),
    DEPENDS_ON_TASKS("depends_on_tasks"),
    PAYLOAD("payload"),
    JOB("job"),
    OPTIONS("options"),
    ENGINE_VERSIONS("engine_versions");

    @Getter
    private final String label;

    Include(final String label) {
        this.label = label;
    }
}