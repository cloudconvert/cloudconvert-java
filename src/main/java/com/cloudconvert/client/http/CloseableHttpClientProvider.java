package com.cloudconvert.client.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class CloseableHttpClientProvider extends AbstractCloseableHttpClientProvider<CloseableHttpClient> {

    public CloseableHttpClientProvider() {
        super(DEFAULT_MAX_PER_ROUTE, MAX_TOTAL);
    }

    public CloseableHttpClientProvider(final int defaultMaxPerRoute, final int maxTotal) {
        super(defaultMaxPerRoute, maxTotal);
    }

    @Override
    public CloseableHttpClient provide() {
        final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(getDefaultMaxPerRoute());
        poolingHttpClientConnectionManager.setMaxTotal(getMaxTotal());

        return HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager).build();
    }
}
