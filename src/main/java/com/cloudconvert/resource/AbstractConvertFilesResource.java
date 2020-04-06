package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public abstract class AbstractConvertFilesResource<TRDAR extends AbstractResult<TaskResponseData>,
    ORP extends AbstractResult<Pageable<OperationResponse>>> extends AbstractResource {

    public static final String PATH_SEGMENT_CONVERT = "convert";
    public static final String PATH_SEGMENT_FORMATS = "formats";

    public AbstractConvertFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a task to convert one input file from input_format to output_format. Requires the task.write scope.
     *
     * @param convertFilesTaskRequest {@link ConvertFilesTaskRequest}
     * @return {@link TRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats() throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters (optional) Filters:
     *                - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                - engine - Filter result to only include conversions with a matching engine name.
     *                - engine_version - Filter result to only include conversions with a matching engine version.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters  (optional) Filters:
     *                 - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                 - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                 - engine - Filter result to only include conversions with a matching engine name.
     *                 - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes (optional) Include options and/or engine_versions in the result.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters     (optional) Filters:
     *                    - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                    - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                    - engine - Filter result to only include conversions with a matching engine name.
     *                    - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes    (optional) Include options and/or engine_versions in the result.
     * @param alternative (optional) For some formats multiple alternative engines are available.
     *                    If set to true, the result includes these alternative conversion types. Default to false.
     * @return {@link ORP}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORP convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException;
}
