package com.cloudconvert.client.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class SyncHttpClientProvider implements HttpClientProvider<CloseableHttpClient> {

    @Override
    public CloseableHttpClient provide() {
        return HttpClientBuilder.create().build();
    }
}
