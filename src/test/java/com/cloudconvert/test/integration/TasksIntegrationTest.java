package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
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
public class TasksIntegrationTest extends AbstractTest {

    private static final String JPG = "jpg";
    private static final String PNG = "png";
    private static final String PDF = "pdf";
    private static final String ZIP = "zip";
    private static final String URL = "https://cloudconvert.com/";

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private CloudConvertClient cloudConvertClient;

    private InputStream jpgTest1InputStream;

    private WaitConditionFactoryProvider waitConditionFactoryProvider;

    @Before
    public void before() throws Exception {
        cloudConvertClient = new CloudConvertClient();

        jpgTest1InputStream = TasksIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);

        waitConditionFactoryProvider = new WaitConditionFactoryProvider();
    }

    @Test
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // List convert formats
        final Result<Pageable<OperationResponse>> convertFormatsOperationResponsePageableResult = cloudConvertClient.tasks().convertFormats();
        assertThat(convertFormatsOperationResponsePageableResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final List<OperationResponse> operationResponses = convertFormatsOperationResponsePageableResult.getBody().getData();
        assertThat(operationResponses).anySatisfy(operationResponse -> assertThat(operationResponse.getOutputFormat()).isEqualTo(JPG));

        // Convert
        final ConvertFilesTaskRequest convertFilesTaskRequest = new ConvertFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponse> convertTaskResponseResult = cloudConvertClient.tasks().convert(convertFilesTaskRequest);
        assertThat(convertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse convertTaskResponse = convertTaskResponseResult.getBody();
        assertThat(convertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);

        // Wait
        final TaskResponse waitConvertTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(convertTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(convertTaskResponse.getId()),
                    awaitTaskResponseResult -> awaitTaskResponseResult.getStatus() == HttpStatus.SC_OK
                ).getBody(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(waitConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Show
        final Result<TaskResponse> showConvertTaskResponseResult = cloudConvertClient.tasks().show(convertTaskResponse.getId());
        assertThat(showConvertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showConvertTaskResponse = showConvertTaskResponseResult.getBody();
        assertThat(showConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(showConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(convertTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void optimizeFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Optimize
        final OptimizeFilesTaskRequest optimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponse> optimizeTaskResponseResult = cloudConvertClient.tasks().optimize(optimizeFilesTaskRequest);
        assertThat(optimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse optimizeTaskResponse = optimizeTaskResponseResult.getBody();
        assertThat(optimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);

        // Wait
        final TaskResponse waitOptimizeTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(optimizeTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(optimizeTaskResponse.getId()),
                    awaitTaskResponseResult -> awaitTaskResponseResult.getStatus() == HttpStatus.SC_OK
                ).getBody(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(waitOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Show
        final Result<TaskResponse> showOptimizeTaskResponseResult = cloudConvertClient.tasks().show(optimizeTaskResponse.getId());
        assertThat(showOptimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showOptimizeTaskResponse = showOptimizeTaskResponseResult.getBody();
        assertThat(showOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(showOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(optimizeTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponse> captureTaskResponseResult = cloudConvertClient.tasks().capture(captureWebsitesTaskRequest);
        assertThat(captureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseResult.getBody();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final TaskResponse waitCaptureTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(captureTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(captureTaskResponse.getId()),
                    awaitTaskResponseResult -> awaitTaskResponseResult.getStatus() == HttpStatus.SC_OK
                ).getBody(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(waitCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Show
        final Result<TaskResponse> showCaptureTaskResponseResult = cloudConvertClient.tasks().show(captureTaskResponse.getId());
        assertThat(showCaptureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showCaptureTaskResponse = showCaptureTaskResponseResult.getBody();
        assertThat(showCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(showCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(captureTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Archive
        final CreateArchivesTaskRequest createArchivesTaskRequest = new CreateArchivesTaskRequest().setInput(uploadImportTaskResponse.getId()).setOutputFormat(ZIP);
        final Result<TaskResponse> archiveTaskResponseResult = cloudConvertClient.tasks().archive(createArchivesTaskRequest);
        assertThat(archiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse archiveTaskResponse = archiveTaskResponseResult.getBody();
        assertThat(archiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);

        // Wait
        final TaskResponse waitArchiveTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(archiveTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(archiveTaskResponse.getId()),
                    awaitTaskResponseResult -> awaitTaskResponseResult.getStatus() == HttpStatus.SC_OK
                ).getBody(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(waitArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Show
        final Result<TaskResponse> showArchiveTaskResponseResult = cloudConvertClient.tasks().show(archiveTaskResponse.getId());
        assertThat(showArchiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showArchiveTaskResponse = showArchiveTaskResponseResult.getBody();
        assertThat(showArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(showArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(archiveTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void executeCommandTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Command
        final ExecuteCommandsTaskRequest executeCommandsTaskRequest = new ExecuteCommandsTaskRequest()
            .setInput(uploadImportTaskResponse.getId()).setEngine(ExecuteCommandsTaskRequest.Engine.GRAPHICSMAGICK)
            .setCommand(ExecuteCommandsTaskRequest.Command.GM).setArguments("version");
        final Result<TaskResponse> executeTaskResponseResult = cloudConvertClient.tasks().command(executeCommandsTaskRequest);
        assertThat(executeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse executeTaskResponse = executeTaskResponseResult.getBody();
        assertThat(executeTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);

        // Wait
        final TaskResponse waitExecuteTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(executeTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(executeTaskResponse.getId()),
                    awaitTaskResponseResult -> awaitTaskResponseResult.getStatus() == HttpStatus.SC_OK
                ).getBody(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(waitExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Show
        final Result<TaskResponse> showExecuteTaskResponseResult = cloudConvertClient.tasks().show(executeTaskResponse.getId());
        assertThat(showExecuteTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showExecuteTaskResponse = showExecuteTaskResponseResult.getBody();
        assertThat(showExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(showExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(executeTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void listTasksLifecycle() throws Exception {
        // List operations
        final Result<Pageable<OperationResponse>> operationResponsePageable = cloudConvertClient.tasks().operations();
        assertThat(operationResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // List tasks
        final Result<Pageable<TaskResponse>> taskResponsePageable = cloudConvertClient.tasks().list(ImmutableMap.of(), ImmutableList.of(), new Pagination(100, 1));
        assertThat(taskResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        cloudConvertClient.close();
    }
}
