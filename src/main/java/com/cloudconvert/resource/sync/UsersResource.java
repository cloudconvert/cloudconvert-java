package com.cloudconvert.resource.sync;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.executor.RequestExecutor;
import com.cloudconvert.resource.AbstractUsersResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class UsersResource extends AbstractUsersResource<Result<UserResponseData>> {

    private final RequestExecutor requestExecutor;

    public UsersResource(
        final SettingsProvider settingsProvider,
        final ObjectMapperProvider objectMapperProvider, final RequestExecutor requestExecutor
    ) {
        super(settingsProvider, objectMapperProvider);

        this.requestExecutor = requestExecutor;
    }

    @Override
    public Result<UserResponseData> me() throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_USERS, PATH_SEGMENT_ME));

        return requestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), USER_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        requestExecutor.close();
    }
}
