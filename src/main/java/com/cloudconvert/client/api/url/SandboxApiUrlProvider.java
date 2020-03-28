package com.cloudconvert.client.api.url;

public class SandboxApiUrlProvider implements ApiUrlProvider {

    private static final String SANDBOX_API_URL = "https://api.sandbox.cloudconvert.com/v2";

    @Override
    public String provide() {
        return SANDBOX_API_URL;
    }
}
