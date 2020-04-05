package com.cloudconvert.client.api.key;

public interface ApiKeyProvider {

    String API_KEY = "CLOUDCONVERT_API_KEY";

    String provide();
}
