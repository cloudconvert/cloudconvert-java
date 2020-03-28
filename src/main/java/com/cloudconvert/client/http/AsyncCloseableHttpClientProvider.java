package com.cloudconvert.client.http;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import java.io.IOException;

public class AsyncCloseableHttpClientProvider extends AbstractCloseableHttpClientProvider<CloseableHttpAsyncClient> {

    public AsyncCloseableHttpClientProvider() {
        super(DEFAULT_MAX_PER_ROUTE, MAX_TOTAL);
    }

    public AsyncCloseableHttpClientProvider(final int defaultMaxPerRoute, final int maxTotal) {
        super(defaultMaxPerRoute, maxTotal);
    }

    @Override
    public CloseableHttpAsyncClient provide() throws IOException {
        final ConnectingIOReactor connectingIOReactor = new DefaultConnectingIOReactor();
        final PoolingNHttpClientConnectionManager poolingNHttpClientConnectionManager = new PoolingNHttpClientConnectionManager(connectingIOReactor);
        poolingNHttpClientConnectionManager.setDefaultMaxPerRoute(getDefaultMaxPerRoute());
        poolingNHttpClientConnectionManager.setMaxTotal(getMaxTotal());

        final CloseableHttpAsyncClient closeableHttpAsyncClient = HttpAsyncClientBuilder.create().setConnectionManager(poolingNHttpClientConnectionManager).build();
        closeableHttpAsyncClient.start();

        return closeableHttpAsyncClient;
    }
}
