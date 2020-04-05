package com.cloudconvert.client.api.key;

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
public class EnvironmentVariableApiKeyProviderTest {

    public static final String API_KEY = "api-key";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void success() {
        environmentVariables.set(ApiKeyProvider.API_KEY, API_KEY);

        assertThat(new EnvironmentVariableApiKeyProvider().provide()).isEqualTo(API_KEY);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new EnvironmentVariableApiKeyProvider().provide()).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Could not resolve " + ApiKeyProvider.API_KEY + " environment variable, make sure it is set ...");
    }
}
