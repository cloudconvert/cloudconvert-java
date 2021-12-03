package com.cloudconvert.dto.result;

import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AsyncResult<T> extends AbstractResult<T> {

    public abstract Result<T> get() throws InterruptedException, ExecutionException, IOException, CloudConvertClientException, CloudConvertServerException;

    public abstract Result<T> get(
        final long timeout, final TimeUnit timeUnit
    ) throws InterruptedException, ExecutionException, TimeoutException, IOException, CloudConvertClientException, CloudConvertServerException;
}
