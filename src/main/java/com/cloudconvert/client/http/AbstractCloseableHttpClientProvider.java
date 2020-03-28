package com.cloudconvert.client.http;

import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractCloseableHttpClientProvider<C extends Closeable> {

    public static final int DEFAULT_MAX_PER_ROUTE = 3;
    public static final int MAX_TOTAL = 37;

    @Getter
    private final int defaultMaxPerRoute;

    @Getter
    private final int maxTotal;

    public AbstractCloseableHttpClientProvider() {
        this(DEFAULT_MAX_PER_ROUTE, MAX_TOTAL);
    }

    public AbstractCloseableHttpClientProvider(final int defaultMaxPerRoute, final int maxTotal) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.maxTotal = maxTotal;
    }

    public abstract C provide() throws IOException;
}
