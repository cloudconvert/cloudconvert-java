package com.cloudconvert.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum Status {

    WAITING("waiting"),
    PROCESSING("processing"),
    FINISHED("finished"),
    ERROR("error");

    @Getter
    @JsonValue
    private final String label;

    Status(final String label) {
        this.label = label;
    }
}