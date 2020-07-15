package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.request.CreateThumbnailsTaskRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.resource.params.converter.AlternativeToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.FiltersToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.IncludesToNameValuePairsConverter;
import com.cloudconvert.resource.params.converter.PaginationToNameValuePairsConverter;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public abstract class AbstractTasksResource<TRAR extends AbstractResult<TaskResponse>,
    TRPAR extends AbstractResult<Pageable<TaskResponse>>, VAR extends AbstractResult<Void>,
    ORPAR extends AbstractResult<Pageable<OperationResponse>>> extends AbstractResource {

    public static final String PATH_SEGMENT_TASKS = "tasks";
    public static final String PATH_SEGMENT_OPERATIONS = "operations";
    public static final String PATH_SEGMENT_WAIT = "wait";
    public static final String PATH_SEGMENT_CANCEL = "cancel";
    public static final String PATH_SEGMENT_RETRY = "retry";

    @Getter
    private final AbstractConvertFilesResource<TRAR, ORPAR> abstractConvertFilesResource;

    @Getter
    private final AbstractOptimizeFilesResource<TRAR> abstractOptimizeFilesResource;

    @Getter
    private final AbstractCaptureWebsitesResource<TRAR> abstractCaptureWebsitesResource;

    @Getter
    private final AbstractMergeFilesResource<TRAR> abstractMergeFilesResource;

    @Getter
    private final AbstractCreateArchivesResource<TRAR> abstractCreateArchivesResource;

    @Getter
    private final AbstractExecuteCommandsResource<TRAR> abstractExecuteCommandsResource;

    @Getter
    private final AbstractCreateThumbnailsResource<TRAR> abstractCreateThumbnailsResource;

    private final IncludesToNameValuePairsConverter includesToNameValuePairsConverter;
    private final FiltersToNameValuePairsConverter filtersToNameValuePairsConverter;
    private final AlternativeToNameValuePairsConverter alternativeToNameValuePairsConverter;
    private final PaginationToNameValuePairsConverter paginationToNameValuePairsConverter;

    public AbstractTasksResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider,
        final AbstractConvertFilesResource<TRAR, ORPAR> abstractConvertFilesResource, final AbstractOptimizeFilesResource<TRAR> abstractOptimizeFilesResource,
        final AbstractCaptureWebsitesResource<TRAR> abstractCaptureWebsitesResource, final AbstractMergeFilesResource<TRAR> abstractMergeFilesResource,
        final AbstractCreateArchivesResource<TRAR> abstractCreateArchivesResource, final AbstractExecuteCommandsResource<TRAR> abstractExecuteCommandsResource,
        final AbstractCreateThumbnailsResource<TRAR> abstractCreateThumbnailsResource
    ) {
        super(settingsProvider, objectMapperProvider);

        this.abstractConvertFilesResource = abstractConvertFilesResource;
        this.abstractOptimizeFilesResource = abstractOptimizeFilesResource;
        this.abstractCaptureWebsitesResource = abstractCaptureWebsitesResource;
        this.abstractMergeFilesResource = abstractMergeFilesResource;
        this.abstractCreateArchivesResource = abstractCreateArchivesResource;
        this.abstractExecuteCommandsResource = abstractExecuteCommandsResource;
        this.abstractCreateThumbnailsResource = abstractCreateThumbnailsResource;

        this.includesToNameValuePairsConverter = new IncludesToNameValuePairsConverter();
        this.filtersToNameValuePairsConverter = new FiltersToNameValuePairsConverter();
        this.alternativeToNameValuePairsConverter = new AlternativeToNameValuePairsConverter();
        this.paginationToNameValuePairsConverter = new PaginationToNameValuePairsConverter();
    }

    /**
     * Show a task. Requires the task.read scope.
     *
     * @param taskId task id
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR show(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException;

    /**
     * Show a task. Requires the task.read scope.
     *
     * @param taskId   task id
     * @param includes (optional) Include retries, depends_on_tasks, payload and/or job in the result. Multiple include values are separated by , (comma).
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR show(
        @NotNull final String taskId, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getShowHttpUriRequest(
        @NotNull final String taskId, @NotNull final List<Include> includes
    ) throws URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(includesToNameValuePairsConverter.convert(includes)).build();
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId), nameValuePairs);

        return getHttpUriRequest(HttpGet.class, uri);
    }

    /**
     * Wait until the task status is finished or error. This makes the request block until the task has been completed. Requires the task.read scope.
     * <p>
     * We do not recommend using this for long running jobs (e.g. video encodings).
     * Your system might automatically time out requests if there is not data transferred for a longer time.
     * <p>
     * In general, please avoid to block your application until a CloudConvert job completes.
     * There might be cases in which we need to queue your task which results in longer processing times than usual.
     * Using an asynchronous approach with webhooks is beneficial in such cases.
     *
     * @param taskId task id
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR wait(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getWaitHttpUriRequest(
        @NotNull final String taskId
    ) throws URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_WAIT));

        return getHttpUriRequest(HttpGet.class, uri);
    }

    /**
     * List all tasks with their status, payload and result. Requires the task.read scope.
     *
     * @return {@link TRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRPAR list() throws IOException, URISyntaxException;

    /**
     * List all tasks with their status, payload and result. Requires the task.read scope.
     *
     * @param filters (optional) Filters:
     *                - job_id - The result will be filtered to include only tasks for a specific Job ID.
     *                - status - The result will be filtered to include only tasks with a specific status (waiting, processing, finished or error).
     *                - operation -  Filter result to only include tasks of with a matching operation (for example convert or import/s3).
     * @return {@link TRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRPAR list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all tasks with their status, payload and result. Requires the task.read scope.
     *
     * @param filters  (optional) Filters:
     *                 - job_id - The result will be filtered to include only tasks for a specific Job ID.
     *                 - status - The result will be filtered to include only tasks with a specific status (waiting, processing, finished or error).
     *                 - operation - Filter result to only include tasks of with a matching operation (for example convert or import/s3).
     * @param includes (optional) Include retries and/or depends_on_tasks in the result.
     * @return {@link TRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRPAR list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    /**
     * List all tasks with their status, payload and result. Requires the task.read scope.
     *
     * @param filters    (optional) Filters:
     *                   - job_id - The result will be filtered to include only tasks for a specific Job ID.
     *                   - status - The result will be filtered to include only tasks with a specific status (waiting, processing, finished or error).
     *                   - operation - Filter result to only include tasks of with a matching operation (for example convert or import/s3).
     * @param includes   (optional) Include retries and/or depends_on_tasks in the result.
     * @param pagination (optional) Pagination:
     *                   - per_page - Number of tasks per page, defaults to 100.
     *                   - page - The result page to show.
     * @return {@link TRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRPAR list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getListHttpUriRequest(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(paginationToNameValuePairsConverter.convert(pagination)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS), nameValuePairs);

        return getHttpUriRequest(HttpGet.class, uri);
    }

    /**
     * Cancel a task that is in status waiting or processing. Requires the task.write scope.
     *
     * @param taskId task id
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR cancel(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getCancelHttpUriRequest(
        @NotNull final String taskId
    ) throws URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_CANCEL));

        return getHttpUriRequest(HttpPost.class, uri);
    }

    /**
     * Create a new task, based on the payload of another task. Requires the task.write scope.
     *
     * @param taskId task id
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR retry(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getRetryHttpUriRequest(
        @NotNull final String taskId
    ) throws URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId, PATH_SEGMENT_RETRY));

        return getHttpUriRequest(HttpPost.class, uri);
    }

    /**
     * Delete a task, including all data. Requires the task.write scope.
     * Tasks are deleted automatically 24 hours after they have ended.
     *
     * @param taskId task id
     * @return {@link VAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract VAR delete(
        @NotNull final String taskId
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getDeleteHttpUriRequest(
        @NotNull final String taskId
    ) throws URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_TASKS, taskId));

        return getHttpUriRequest(HttpDelete.class, uri);
    }

    /**
     * List all possible operations, formats, engines and possible options.
     *
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR operations() throws IOException, URISyntaxException;

    /**
     * List all possible operations, formats, engines and possible options.
     *
     * @param filters (optional) Filters:
     *                - operation - The result will be filtered to include only possible operations with a matching operation name (e.g. convert or optimize).
     *                - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                - engine - Filter result to only include conversions with a matching engine name.
     *                - engine_version - Filter result to only include conversions with a matching engine version.
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR operations(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all possible operations, formats, engines and possible options.
     *
     * @param filters  (optional) Filters:
     *                 - operation - The result will be filtered to include only possible operations with a matching operation name (e.g. convert or optimize).
     *                 - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                 - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                 - engine - Filter result to only include conversions with a matching engine name.
     *                 - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes (optional) Include options and/or engine_versions in the result.
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    /**
     * List all possible operations, formats, engines and possible options.
     *
     * @param filters     (optional) Filters:
     *                    - operation - The result will be filtered to include only possible operations with a matching operation name (e.g. convert or optimize).
     *                    - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                    - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                    - engine - Filter result to only include conversions with a matching engine name.
     *                    - engine_version - Filter result to only include conversions with a matching engine version.
     * @param includes    (optional) Include options and/or engine_versions in the result.
     * @param alternative (optional) For some formats multiple alternative engines are available.
     *                    If set to true, the result includes these alternative conversion types. Default to false.
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR operations(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getOperationsHttpUriRequest(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws URISyntaxException {
        final List<NameValuePair> nameValuePairs = ImmutableList.<NameValuePair>builder().addAll(filtersToNameValuePairsConverter.convert(filters))
            .addAll(includesToNameValuePairsConverter.convert(includes)).addAll(alternativeToNameValuePairsConverter.convert(alternative)).build();

        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_OPERATIONS), nameValuePairs);

        return getHttpUriRequest(HttpGet.class, uri);
    }

    /**
     * Create a task to convert one input file from input_format to output_format. Requires the task.write scope.
     *
     * @param convertFilesTaskRequest {@link ConvertFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR convert(
        @NotNull final ConvertFilesTaskRequest convertFilesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR convertFormats() throws IOException, URISyntaxException;

    /**
     * List all supported formats, their engines and possible options.
     *
     * @param filters (optional) Filters:
     *                - input_format - The result will be filtered to include only possible conversions with a matching input format.
     *                - output_format - The result will be filtered to include only possible conversions with a matching output format.
     *                - engine - Filter result to only include conversions with a matching engine name.
     *                - engine_version - Filter result to only include conversions with a matching engine version.
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR convertFormats(
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
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR convertFormats(
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
     * @return {@link ORPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract ORPAR convertFormats(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Boolean alternative
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to optimize and compress a file. Currently supported formats are PDF, PNG and JPG.
     *
     * @param optimizeFilesTaskRequest {@link OptimizeFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR optimize(
        @NotNull final OptimizeFilesTaskRequest optimizeFilesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to convert a website to PDF or to capture a screenshot of a website (png, jpg).
     *
     * @param captureWebsitesTaskRequest {@link CaptureWebsitesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR capture(
        @NotNull final CaptureWebsitesTaskRequest captureWebsitesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to merge at least two files to one PDF. If input files are not PDFs yet, they are automatically converted to PDF.
     *
     * @param mergeFilesTaskRequest {@link MergeFilesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR merge(
        @NotNull final MergeFilesTaskRequest mergeFilesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to create a ZIP, RAR, 7Z, TAR, TAR.GZ or TAR.BZ2 archive.
     *
     * @param createArchivesTaskRequest {@link CreateArchivesTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR archive(
        @NotNull final CreateArchivesTaskRequest createArchivesTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to execute a command. Currently, ffmpeg, imagemagick and graphicsmagick commands re supported.
     * You can access the files from the input task in the /input/{taskName}/ (For example: /input/import-1/) directory.
     * All files that are created in the /output/ directory are available for following tasks (e.g. export tasks).
     *
     * @param executeCommandsTaskRequest {@link ExecuteCommandsTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR command(
        @NotNull final ExecuteCommandsTaskRequest executeCommandsTaskRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to convert one input file from input_format to output_format. Requires the task.write scope.
     *
     * @param createThumbnailsTaskRequest {@link CreateThumbnailsTaskRequest}
     * @return {@link TRAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR thumbnail(
            @NotNull final CreateThumbnailsTaskRequest createThumbnailsTaskRequest
    ) throws IOException, URISyntaxException;


    @Override
    public void close() throws IOException {
        abstractConvertFilesResource.close();
        abstractOptimizeFilesResource.close();
        abstractCaptureWebsitesResource.close();
        abstractMergeFilesResource.close();
        abstractCreateArchivesResource.close();
        abstractExecuteCommandsResource.close();
        abstractCreateThumbnailsResource.close();
    }
}
