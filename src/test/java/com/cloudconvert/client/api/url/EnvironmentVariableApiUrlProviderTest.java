package com.cloudconvert.client.api.url;

import com.cloudconvert.test.framework.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentVariableApiUrlProviderTest {

    public static final String API_URL = "api-url";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void success() {
        environmentVariables.set(ApiUrlProvider.API_URL, API_URL);

        assertThat(new EnvironmentVariableApiUrlProvider().provide()).isEqualTo(API_URL);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new EnvironmentVariableApiUrlProvider().provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiUrlProvider.API_URL + " environment variable, make sure it is set ...");
    }
}
