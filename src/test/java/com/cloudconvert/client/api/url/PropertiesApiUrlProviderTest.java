package com.cloudconvert.client.api.url;

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
public class PropertiesApiUrlProviderTest {

    public static final String API_URL = "api-url";

    @Test
    public void success() {
        final Properties properties = new Properties();
        properties.setProperty(ApiUrlProvider.API_URL, API_URL);

        assertThat(new PropertiesApiUrlProvider(properties).provide()).isEqualTo(API_URL);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertiesApiUrlProvider(new Properties()).provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiUrlProvider.API_URL + " property, make sure it is set ...");
    }
}
