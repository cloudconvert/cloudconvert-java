package com.cloudconvert.dto.result;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Builder
public class Result<T> extends AbstractResult<T> {

    @Getter
    private Status status;

    @Getter
    private Map<String, String> headers;

    @Getter
    @Nullable
    private T body;
}
