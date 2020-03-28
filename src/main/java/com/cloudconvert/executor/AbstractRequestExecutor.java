package com.cloudconvert.executor;

import com.cloudconvert.client.http.AbstractCloseableHttpClientProvider;
import com.cloudconvert.extractor.ResultExtractor;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractRequestExecutor<P extends AbstractCloseableHttpClientProvider<C>, C extends Closeable> implements Closeable {

    @Getter
    private final ResultExtractor resultExtractor;

    @Getter
    private final C closeableHttpClient;

    public AbstractRequestExecutor(
        final ResultExtractor resultExtractor, final P closeableHttpClientProvider
    ) throws IOException {
        this.resultExtractor = resultExtractor;
        this.closeableHttpClient = closeableHttpClientProvider.provide();
    }

    @Override
    public void close() throws IOException {
        closeableHttpClient.close();
    }
}
