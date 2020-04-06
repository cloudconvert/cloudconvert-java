package com.cloudconvert.test.integration;

import com.cloudconvert.client.AsyncCloudConvertClient;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.CaptureWebsitesTaskRequest;
import com.cloudconvert.dto.request.ConvertFilesTaskRequest;
import com.cloudconvert.dto.request.CreateArchivesTaskRequest;
import com.cloudconvert.dto.request.ExecuteCommandsTaskRequest;
import com.cloudconvert.dto.request.OptimizeFilesTaskRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.cloudconvert.test.framework.WaitConditionFactoryProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class AsyncTasksIntegrationTest extends AbstractTest {

    private static final String JPG = "jpg";
    private static final String PNG = "png";
    private static final String PDF = "pdf";
    private static final String ZIP = "zip";
    private static final String URL = "https://cloudconvert.com/";

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private AsyncCloudConvertClient asyncCloudConvertClient;

    private InputStream jpgTest1InputStream;

    private WaitConditionFactoryProvider waitConditionFactoryProvider;

    @Before
    public void before() throws Exception {
        asyncCloudConvertClient = new AsyncCloudConvertClient();

        jpgTest1InputStream = AsyncTasksIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);

        waitConditionFactoryProvider = new WaitConditionFactoryProvider();
    }

    @Test
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // List convert formats
        final Result<Pageable<OperationResponse>> convertFormatsOperationResponsePageableResult = asyncCloudConvertClient.tasks().convertFormats().get();
        assertThat(convertFormatsOperationResponsePageableResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final List<OperationResponse> operationResponses = convertFormatsOperationResponsePageableResult.getBody().get().getData();
        assertThat(operationResponses).anySatisfy(operationResponse -> assertThat(operationResponse.getOutputFormat()).isEqualTo(JPG));

        // Convert
        final ConvertFilesTaskRequest convertFilesTaskRequest = new ConvertFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponseData> convertTaskResponseDataResult = asyncCloudConvertClient.tasks().convert(convertFilesTaskRequest).get();
        assertThat(convertTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse convertTaskResponse = convertTaskResponseDataResult.getBody().get().getData();
        assertThat(convertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);

        // Wait
        final TaskResponse waitConvertTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(convertTaskResponse.getId()).until(
                    () -> asyncCloudConvertClient.tasks().wait(convertTaskResponse.getId()).get(),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(waitConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showConvertTaskResponseDataResult = asyncCloudConvertClient.tasks().show(convertTaskResponse.getId()).get();
        assertThat(showConvertTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showConvertTaskResponse = showConvertTaskResponseDataResult.getBody().get().getData();
        assertThat(showConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(showConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(convertTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void optimizeFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Optimize
        final OptimizeFilesTaskRequest optimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponseData> optimizeTaskResponseDataResult = asyncCloudConvertClient.tasks().optimize(optimizeFilesTaskRequest).get();
        assertThat(optimizeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse optimizeTaskResponse = optimizeTaskResponseDataResult.getBody().get().getData();
        assertThat(optimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);

        // Wait
        final TaskResponse waitOptimizeTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(optimizeTaskResponse.getId()).until(
                    () -> asyncCloudConvertClient.tasks().wait(optimizeTaskResponse.getId()).get(),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(waitOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showOptimizeTaskResponseDataResult = asyncCloudConvertClient.tasks().show(optimizeTaskResponse.getId()).get();
        assertThat(showOptimizeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showOptimizeTaskResponse = showOptimizeTaskResponseDataResult.getBody().get().getData();
        assertThat(showOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(showOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(optimizeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponseData> captureTaskResponseDataResult = asyncCloudConvertClient.tasks().capture(captureWebsitesTaskRequest).get();
        assertThat(captureTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseDataResult.getBody().get().getData();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final TaskResponse waitCaptureTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(captureTaskResponse.getId()).until(
                    () -> asyncCloudConvertClient.tasks().wait(captureTaskResponse.getId()).get(),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(waitCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showCaptureTaskResponseDataResult = asyncCloudConvertClient.tasks().show(captureTaskResponse.getId()).get();
        assertThat(showCaptureTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showCaptureTaskResponse = showCaptureTaskResponseDataResult.getBody().get().getData();
        assertThat(showCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(showCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(captureTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Archive
        final CreateArchivesTaskRequest createArchivesTaskRequest = new CreateArchivesTaskRequest()
            .setInput(uploadImportTaskResponse.getId()).setOutputFormat(ZIP);
        final Result<TaskResponseData> archiveTaskResponseDataResult = asyncCloudConvertClient.tasks().archive(createArchivesTaskRequest).get();
        assertThat(archiveTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse archiveTaskResponse = archiveTaskResponseDataResult.getBody().get().getData();
        assertThat(archiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);

        // Wait
        final TaskResponse waitArchiveTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(archiveTaskResponse.getId()).until(
                    () -> asyncCloudConvertClient.tasks().wait(archiveTaskResponse.getId()).get(),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(waitArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showArchiveTaskResponseDataResult = asyncCloudConvertClient.tasks().show(archiveTaskResponse.getId()).get();
        assertThat(showArchiveTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showArchiveTaskResponse = showArchiveTaskResponseDataResult.getBody().get().getData();
        assertThat(showArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(showArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(archiveTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void executeCommandTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Command
        final ExecuteCommandsTaskRequest executeCommandsTaskRequest = new ExecuteCommandsTaskRequest()
            .setInput(uploadImportTaskResponse.getId()).setEngine(ExecuteCommandsTaskRequest.Engine.GRAPHICSMAGICK)
            .setCommand(ExecuteCommandsTaskRequest.Command.GM).setArguments("version");
        final Result<TaskResponseData> executeTaskResponseDataResult = asyncCloudConvertClient.tasks().command(executeCommandsTaskRequest).get();
        assertThat(executeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse executeTaskResponse = executeTaskResponseDataResult.getBody().get().getData();
        assertThat(executeTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);

        // Wait
        final TaskResponse waitExecuteTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(executeTaskResponse.getId()).until(
                    () -> asyncCloudConvertClient.tasks().wait(executeTaskResponse.getId()).get(),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(waitExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showExecuteTaskResponseDataResult = asyncCloudConvertClient.tasks().show(executeTaskResponse.getId()).get();
        assertThat(showExecuteTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showExecuteTaskResponse = showExecuteTaskResponseDataResult.getBody().get().getData();
        assertThat(showExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(showExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(executeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void listTasksLifecycle() throws Exception {
        // List operations
        final Result<Pageable<OperationResponse>> operationResponsePageable = asyncCloudConvertClient.tasks().operations().get();
        assertThat(operationResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // List tasks
        final Result<Pageable<TaskResponse>> taskResponsePageable = asyncCloudConvertClient.tasks()
            .list(ImmutableMap.of(), ImmutableList.of(), new Pagination(100, 1)).get();
        assertThat(taskResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        asyncCloudConvertClient.close();
    }
}
