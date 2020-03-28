package com.cloudconvert.resource;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.result.AbstractResult;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractUsersResource<URDAR extends AbstractResult<UserResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_USERS = "users";
    public static final String PATH_SEGMENT_ME = "me";

    public AbstractUsersResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);
    }

    /**
     * Show the current user. Requires the user.read scope.
     *
     * @return {@link URDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract URDAR me() throws IOException, URISyntaxException;
}
