package com.cloudconvert.client.api.key;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class StringApiKeyProviderTest {

    public static final String API_KEY = "api-key";

    @Test
    public void success() {
        assertThat(new StringApiKeyProvider(API_KEY).provide()).isEqualTo(API_KEY);
    }
}
