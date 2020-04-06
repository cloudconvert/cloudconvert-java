package com.cloudconvert.test.unit.settings;

import com.cloudconvert.client.setttings.AbstractSettingsProvider;
import com.cloudconvert.client.setttings.PropertySettingsProvider;
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
public class PropertySettingsProviderTest {

    public static final String API_KEY = "api-key";
    public static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";

    @Test
    public void success_useSandbox() {
        final Properties properties = new Properties();
        properties.setProperty(AbstractSettingsProvider.API_KEY, API_KEY);
        properties.setProperty(AbstractSettingsProvider.USE_SANDBOX, "true");
        properties.setProperty(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final PropertySettingsProvider propertySettingsProvider = new PropertySettingsProvider(properties);
        assertThat(propertySettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(propertySettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(propertySettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_SANDBOX);
    }

    @Test
    public void success_useLive() {
        final Properties properties = new Properties();
        properties.setProperty(AbstractSettingsProvider.API_KEY, API_KEY);
        properties.setProperty(AbstractSettingsProvider.USE_SANDBOX, "false");
        properties.setProperty(AbstractSettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final PropertySettingsProvider propertySettingsProvider = new PropertySettingsProvider(properties);
        assertThat(propertySettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(propertySettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(propertySettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_LIVE);
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> new PropertySettingsProvider(new Properties())).isInstanceOf(IllegalArgumentException.class);
    }
}
