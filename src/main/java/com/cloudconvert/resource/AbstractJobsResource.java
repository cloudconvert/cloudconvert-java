package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.JobResponseData;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.result.AbstractResult;
import com.cloudconvert.resource.params.Filter;
import com.cloudconvert.resource.params.Include;
import com.cloudconvert.resource.params.Pagination;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public abstract class AbstractJobsResource<JRDAR extends AbstractResult<JobResponseData>,
    JRPAR extends AbstractResult<Pageable<JobResponse>>, VAR extends AbstractResult<Void>> extends AbstractResource {

    public static final String PATH_SEGMENT_JOBS = "jobs";
    public static final String PATH_SEGMENT_WAIT = "wait";

    public AbstractJobsResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * Create a job with one ore more tasks. Requires the task.write scope.
     *
     * @param tasks The example on the right consists of three tasks: import-my-file, convert-my-file and export-my-file.
     *              You can name these tasks however you want, but only alphanumeric characters, - and _ are allowed in the task names.
     *              <p>
     *              Each task has a operation, which is the endpoint for creating the task (for example: convert, import/s3 or export/s3).
     *              The other parameters are the same as for creating the task using their direct endpoint.
     *              The input parameter allows it to directly reference the name of another task, created with the same job request.
     * @return {@link JRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRDAR create(
        @NotNull final Map<String, TaskRequest> tasks
    ) throws IOException, URISyntaxException;


    /**
     * Create a job with one ore more tasks. Requires the task.write scope.
     *
     * @param tasks The example on the right consists of three tasks: import-my-file, convert-my-file and export-my-file.
     *              You can name these tasks however you want, but only alphanumeric characters, - and _ are allowed in the task names.
     *              <p>
     *              Each task has a operation, which is the endpoint for creating the task (for example: convert, import/s3 or export/s3).
     *              The other parameters are the same as for creating the task using their direct endpoint.
     *              The input parameter allows it to directly reference the name of another task, created with the same job request.
     * @param tag   An arbitrary string to identify the job.
     *              Does not have any effect and can be used to associate the job with an ID in your application.
     * @return {@link JRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRDAR create(
        @NotNull final Map<String, TaskRequest> tasks, @NotNull final String tag
    ) throws IOException, URISyntaxException;

    /**
     * Show a job. Requires the task.read scope.
     *
     * @param jobId job id
     * @return {@link JRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRDAR show(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException;

    /**
     * Wait until the job status is finished or error. This makes the request block until the job has been completed. Requires the task.read scope.
     * <p>
     * We do not recommend using this for long running jobs (e.g. video encodings).
     * Your system might automatically time out requests if there is not data transferred for a longer time.
     * <p>
     * In general, please avoid to block your application until a CloudConvert job completes.
     * There might be cases in which we need to queue your job which results in longer processing times than usual.
     * Using an asynchronous approach with webhooks is beneficial in such cases.
     *
     * @param jobId job id
     * @return {@link JRDAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRDAR wait(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException;

    /**
     * List all jobs. Requires the task.read scope.
     *
     * @return {@link JRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRPAR list() throws IOException, URISyntaxException;

    /**
     * List all jobs. Requires the task.read scope.
     *
     * @param filters (optional) Filters:
     *                - status - The result will be filtered to include only jobs with a specific status (processing, finished or error).
     *                - tag - The result will be filtered to include only jobs with a tag.
     * @return {@link JRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRPAR list(
        @NotNull final Map<Filter, String> filters
    ) throws IOException, URISyntaxException;

    /**
     * List all jobs. Requires the task.read scope.
     *
     * @param filters  (optional) Filters:
     *                 - status - The result will be filtered to include only jobs with a specific status (processing, finished or error).
     *                 - tag - The result will be filtered to include only jobs with a tag.
     * @param includes (optional) Include tasks in the result.
     * @return {@link JRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRPAR list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes
    ) throws IOException, URISyntaxException;

    /**
     * List all jobs. Requires the task.read scope.
     *
     * @param filters    (optional) Filters:
     *                   - status - The result will be filtered to include only jobs with a specific status (processing, finished or error).
     *                   - tag - The result will be filtered to include only jobs with a tag.
     * @param includes   (optional) Include tasks in the result.
     * @param pagination (optional) Pagination:
     *                   - per_page - Number of tasks per page, defaults to 100.
     *                   - page - The result page to show.
     * @return {@link JRPAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract JRPAR list(
        @NotNull final Map<Filter, String> filters, @NotNull final List<Include> includes, @Nullable final Pagination pagination
    ) throws IOException, URISyntaxException;

    /**
     * Delete a job, including all tasks and data. Requires the task.write scope.
     * Jobs are deleted automatically 24 hours after they have ended.
     *
     * @param jobId job id
     * @return {@link VAR}
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract VAR delete(
        @NotNull final String jobId
    ) throws IOException, URISyntaxException;
}
