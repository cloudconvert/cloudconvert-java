package com.cloudconvert.client.api.key;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class PropertiesApiKeyProviderTest {

    public static final String API_KEY = "api-key";

    @Test
    public void success() {
        final Properties properties = new Properties();
        properties.setProperty(ApiKeyProvider.API_KEY, API_KEY);

        assertThat(new PropertiesApiKeyProvider(properties).provide()).isEqualTo(API_KEY);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertiesApiKeyProvider(new Properties()).provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiKeyProvider.API_KEY + " property, make sure it is set ...");
    }
}
