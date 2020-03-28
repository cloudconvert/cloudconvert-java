package com.cloudconvert.client.api.url;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class StringApiUrlProviderTest {

    public static final String API_URL = "api-url";

    @Test
    public void success() {
        assertThat(new StringApiUrlProvider(API_URL).provide()).isEqualTo(API_URL);
    }
}
