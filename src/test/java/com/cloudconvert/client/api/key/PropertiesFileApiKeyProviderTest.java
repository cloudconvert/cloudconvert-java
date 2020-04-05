package com.cloudconvert.client.api.key;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class PropertiesFileApiKeyProviderTest {

    public static final String API_KEY = "api-key";

    @Test
    public void success() throws Exception {
        assertThat(new PropertiesFileApiKeyProvider("test-not-empty-application.properties").provide()).isEqualTo(API_KEY);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertiesFileApiKeyProvider("test-empty-application.properties").provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiKeyProvider.API_KEY + " property, make sure it is set ...");
    }
}
