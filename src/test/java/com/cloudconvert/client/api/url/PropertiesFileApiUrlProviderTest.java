package com.cloudconvert.client.api.url;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class PropertiesFileApiUrlProviderTest {

    public static final String API_URL = "api-url";

    @Test
    public void success() throws Exception {
        assertThat(new PropertiesFileApiUrlProvider("test-not-empty-application.properties").provide()).isEqualTo(API_URL);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertiesFileApiUrlProvider("test-empty-application.properties").provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiUrlProvider.API_URL + " property, make sure it is set ...");
    }
}
