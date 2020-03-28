package com.cloudconvert.resource.async;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.response.UserResponseData;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.executor.AsyncRequestExecutor;
import com.cloudconvert.resource.AbstractUsersResource;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class AsyncUsersResource extends AbstractUsersResource<AsyncResult<UserResponseData>> {

    private final AsyncRequestExecutor asyncRequestExecutor;

    public AsyncUsersResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider,
        final ObjectMapperProvider objectMapperProvider, final AsyncRequestExecutor asyncRequestExecutor
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);

        this.asyncRequestExecutor = asyncRequestExecutor;
    }

    @Override
    public AsyncResult<UserResponseData> me() throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_USERS, PATH_SEGMENT_ME));

        return asyncRequestExecutor.execute(getHttpUriRequest(HttpGet.class, uri), USER_RESPONSE_DATA_TYPE_REFERENCE);
    }

    @Override
    public void close() throws IOException {
        asyncRequestExecutor.close();
    }
}
