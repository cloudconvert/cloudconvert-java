package com.cloudconvert.client.api.key;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class SystemPropertyApiKeyProviderTest {

    public static final String API_KEY = "api-key";

    @Before
    public void before() {
        System.clearProperty(ApiKeyProvider.API_KEY);
    }

    @Test
    public void success() {
        System.setProperty(ApiKeyProvider.API_KEY, API_KEY);

        assertThat(new SystemPropertyApiKeyProvider().provide()).isEqualTo(API_KEY);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new SystemPropertyApiKeyProvider().provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiKeyProvider.API_KEY + " system property, make sure it is set ...");
    }
}
