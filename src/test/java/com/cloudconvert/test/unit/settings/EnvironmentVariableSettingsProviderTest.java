package com.cloudconvert.test.unit.settings;

import com.cloudconvert.client.setttings.AbstractSettingsProvider;
import com.cloudconvert.client.setttings.EnvironmentVariableSettingsProvider;
import com.cloudconvert.client.setttings.EnvironmentVariables;
import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentVariableSettingsProviderTest {

    public static final String API_KEY = "api-key";
    public static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";

    @Mock
    private EnvironmentVariables environmentVariables;

    @Test
    public void success_useSandbox() {
        when(environmentVariables.getenv(AbstractSettingsProvider.API_KEY)).thenReturn(API_KEY);
        when(environmentVariables.getenv(AbstractSettingsProvider.USE_SANDBOX)).thenReturn("true");
        when(environmentVariables.getenv(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET)).thenReturn(WEBHOOK_SIGNING_SECRET);

        final EnvironmentVariableSettingsProvider environmentVariableSettingsProvider = new EnvironmentVariableSettingsProvider(environmentVariables);
        assertThat(environmentVariableSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(environmentVariableSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(environmentVariableSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_SANDBOX);
    }

    @Test
    public void success_useLive() {
        when(environmentVariables.getenv(AbstractSettingsProvider.API_KEY)).thenReturn(API_KEY);
        when(environmentVariables.getenv(AbstractSettingsProvider.USE_SANDBOX)).thenReturn("false");
        when(environmentVariables.getenv(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET)).thenReturn(WEBHOOK_SIGNING_SECRET);

        final EnvironmentVariableSettingsProvider environmentVariableSettingsProvider = new EnvironmentVariableSettingsProvider(environmentVariables);
        assertThat(environmentVariableSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(environmentVariableSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(environmentVariableSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_LIVE);
    }

    @Test
    public void failure() {
        when(environmentVariables.getenv(AbstractSettingsProvider.API_KEY)).thenReturn(null);
        assertThatThrownBy(() -> new EnvironmentVariableSettingsProvider(environmentVariables)).isInstanceOf(IllegalArgumentException.class);
    }
}
