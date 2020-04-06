package com.cloudconvert.test.unit.settings;

import com.cloudconvert.client.setttings.AbstractSettingsProvider;
import com.cloudconvert.client.setttings.PropertyFileSettingsProvider;
import com.cloudconvert.test.framework.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class PropertyFileSettingsProviderTest {

    public static final String API_KEY = "api-key";
    public static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";

    @Test
    public void success_useSandbox() throws IOException {
        final PropertyFileSettingsProvider propertyFileSettingsProvider = new PropertyFileSettingsProvider("test-not-empty-sandbox-application.properties");

        assertThat(propertyFileSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(propertyFileSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(propertyFileSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_SANDBOX);
    }

    @Test
    public void success_useLive() throws IOException {
        final PropertyFileSettingsProvider propertyFileSettingsProvider = new PropertyFileSettingsProvider("test-not-empty-live-application.properties");

        assertThat(propertyFileSettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(propertyFileSettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(propertyFileSettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_LIVE);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertyFileSettingsProvider("test-empty-application.properties")).isInstanceOf(IllegalArgumentException.class);
    }
}
