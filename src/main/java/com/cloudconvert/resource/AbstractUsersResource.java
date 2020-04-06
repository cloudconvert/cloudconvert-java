package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.result.AbstractResult;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractUsersResource<URDAR extends AbstractResult<UserResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_USERS = "users";
    public static final String PATH_SEGMENT_ME = "me";

    public AbstractUsersResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
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
