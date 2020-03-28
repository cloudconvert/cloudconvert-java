package com.cloudconvert.dto.result;

import lombok.Builder;

import java.util.concurrent.TimeUnit;

@Builder
public class CompletedAsyncResult<T> extends AsyncResult<T> {

    private Result<T> result;

    @Override
    public Result<T> get() {
        return result;
    }

    @Override
    public Result<T> get(
        final long timeout, final TimeUnit timeUnit
    ) {
        return result;
    }
}
