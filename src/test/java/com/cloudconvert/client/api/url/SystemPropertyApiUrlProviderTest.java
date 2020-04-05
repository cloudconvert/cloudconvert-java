package com.cloudconvert.client.api.url;

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
public class SystemPropertyApiUrlProviderTest {

    public static final String API_URL = "api-url";

    @Before
    public void before() {
        System.clearProperty(ApiUrlProvider.API_URL);
    }

    @Test
    public void success() {
        System.setProperty(ApiUrlProvider.API_URL, API_URL);

        assertThat(new SystemPropertyApiUrlProvider().provide()).isEqualTo(API_URL);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new SystemPropertyApiUrlProvider().provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiUrlProvider.API_URL + " system property, make sure it is set ...");
    }
}
