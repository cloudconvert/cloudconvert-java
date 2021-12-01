package com.cloudconvert.dto.result;

import com.cloudconvert.exception.CloudConvertClientException;
import com.cloudconvert.exception.CloudConvertServerException;
import com.cloudconvert.extractor.ResultExtractor;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Builder;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Builder
public class FutureAsyncResult<T> extends AsyncResult<T> {

    private ResultExtractor resultExtractor;

    private Future<HttpResponse> future;

    private TypeReference<T> typeReference;

    @Nullable
    private Result<T> result;

    public Result<T> get() throws InterruptedException, ExecutionException, IOException, CloudConvertClientException, CloudConvertServerException {
        return extractResult(future.get());
    }

    public Result<T> get(
        final long timeout, final TimeUnit timeUnit
    ) throws InterruptedException, ExecutionException, TimeoutException, IOException, CloudConvertClientException, CloudConvertServerException {
        return extractResult(future.get(timeout, timeUnit));
    }

    /**
     * This method should be synchronized in order to prevent unexpected behavior if multiple threads tries to access an input stream
     */
    private synchronized Result<T> extractResult(
        final HttpResponse httpResponse
    ) throws IOException, CloudConvertClientException, CloudConvertServerException {
        // Cache result, in case user will try to call get() multiple times
        if (result == null) {
            result = resultExtractor.extract(httpResponse, typeReference);
        }
        return result;
    }
}
