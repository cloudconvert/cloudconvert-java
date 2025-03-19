package com.cloudconvert.test.framework;

import com.cloudconvert.resource.AbstractResource;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public abstract class AbstractTest {

    public static final String API_URL = "https://api.sandbox.cloudconvert.com";
    public static final String API_SYNC_URL = "https://sync.api.sandbox.cloudconvert.com";
    public static final String API_KEY = "api-key";

    public static final String VALUE_AUTHORIZATION = AbstractResource.BEARER + " " + API_KEY;

    public static final long TIMEOUT = 300000L;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }
}
