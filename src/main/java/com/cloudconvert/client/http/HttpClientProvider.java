package com.cloudconvert.client.http;

import java.io.Closeable;

public interface HttpClientProvider<C extends Closeable> {

    C provide();
}
