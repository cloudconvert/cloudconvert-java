package com.cloudconvert.test.unit.settings;

import com.cloudconvert.client.setttings.AbstractSettingsProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.client.setttings.SystemPropertySettingsProvider;
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
public class SystemPropertySettingsProviderTest {

    public static final String API_KEY = "api-key";
    public static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";

    @Before
    public void before() {
        System.clearProperty(SettingsProvider.API_KEY);
        System.clearProperty(SettingsProvider.WEBHOOK_SIGNING_SECRET);
    }

    @Test
    public void success_useSandbox() {
        System.setProperty(SettingsProvider.API_KEY, API_KEY);
        System.setProperty(SettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final SystemPropertySettingsProvider systemPropertySettingsProvider = new SystemPropertySettingsProvider(true);
        assertThat(systemPropertySettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(systemPropertySettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(systemPropertySettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_SANDBOX);
    }

    @Test
    public void success_useLive() {
        System.setProperty(SettingsProvider.API_KEY, API_KEY);
        System.setProperty(SettingsProvider.WEBHOOK_SIGNING_SECRET, WEBHOOK_SIGNING_SECRET);

        final SystemPropertySettingsProvider systemPropertySettingsProvider = new SystemPropertySettingsProvider();
        assertThat(systemPropertySettingsProvider.getApiKey()).isEqualTo(API_KEY);
        assertThat(systemPropertySettingsProvider.getWebhookSigningSecret()).isEqualTo(WEBHOOK_SIGNING_SECRET);
        assertThat(systemPropertySettingsProvider.getApiUrl()).isEqualTo(AbstractSettingsProvider.API_URL_LIVE);
    }

    @Test
    public void failure() {
        assertThatThrownBy(SystemPropertySettingsProvider::new).isInstanceOf(IllegalArgumentException.class);
    }
}
