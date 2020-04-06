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
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
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

    @Before
    public void before() throws Exception {
        cloudConvertClient = new CloudConvertClient(true);

        jpgTest1InputStream = TasksIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // List convert formats
        final Result<Pageable<OperationResponse>> convertFormatsOperationResponsePageableResult = cloudConvertClient.tasks().convertFormats();
        assertThat(convertFormatsOperationResponsePageableResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final List<OperationResponse> operationResponses = convertFormatsOperationResponsePageableResult.getBody().get().getData();
        assertThat(operationResponses).anySatisfy(operationResponse -> assertThat(operationResponse.getOutputFormat()).isEqualTo(JPG));

        // Convert
        final ConvertFilesTaskRequest convertFilesTaskRequest = new ConvertFilesTaskRequest().setInput(importUploadTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponseData> convertTaskResponseDataResult = cloudConvertClient.tasks().convert(convertFilesTaskRequest);
        assertThat(convertTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse convertTaskResponse = convertTaskResponseDataResult.getBody().get().getData();
        assertThat(convertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = cloudConvertClient.tasks().wait(convertTaskResponse.getId());
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = cloudConvertClient.tasks().show(convertTaskResponse.getId());
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(convertTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void optimizeFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Optimize
        final OptimizeFilesTaskRequest optimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput(importUploadTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponseData> optimizeTaskResponseDataResult = cloudConvertClient.tasks().optimize(optimizeFilesTaskRequest);
        assertThat(optimizeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse optimizeTaskResponse = optimizeTaskResponseDataResult.getBody().get().getData();
        assertThat(optimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = cloudConvertClient.tasks().wait(optimizeTaskResponse.getId());
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = cloudConvertClient.tasks().show(optimizeTaskResponse.getId());
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(optimizeTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponseData> captureTaskResponseDataResult = cloudConvertClient.tasks().capture(captureWebsitesTaskRequest);
        assertThat(captureTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseDataResult.getBody().get().getData();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = cloudConvertClient.tasks().wait(captureTaskResponse.getId());
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = cloudConvertClient.tasks().show(captureTaskResponse.getId());
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(captureTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Archive
        final CreateArchivesTaskRequest createArchivesTaskRequest = new CreateArchivesTaskRequest().setInput(importUploadTaskResponse.getId()).setOutputFormat(ZIP);
        final Result<TaskResponseData> archiveTaskResponseDataResult = cloudConvertClient.tasks().archive(createArchivesTaskRequest);
        assertThat(archiveTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse archiveTaskResponse = archiveTaskResponseDataResult.getBody().get().getData();
        assertThat(archiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = cloudConvertClient.tasks().wait(archiveTaskResponse.getId());
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = cloudConvertClient.tasks().show(archiveTaskResponse.getId());
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(archiveTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void executeCommandTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Command
        final ExecuteCommandsTaskRequest executeCommandsTaskRequest = new ExecuteCommandsTaskRequest()
            .setInput(importUploadTaskResponse.getId()).setEngine(ExecuteCommandsTaskRequest.Engine.GRAPHICSMAGICK)
            .setCommand(ExecuteCommandsTaskRequest.Command.GM).setArguments("version");
        final Result<TaskResponseData> executeTaskResponseDataResult = cloudConvertClient.tasks().command(executeCommandsTaskRequest);
        assertThat(executeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse executeTaskResponse = executeTaskResponseDataResult.getBody().get().getData();
        assertThat(executeTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = cloudConvertClient.tasks().wait(executeTaskResponse.getId());
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = cloudConvertClient.tasks().show(executeTaskResponse.getId());
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(executeTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void listAndDeleteTasksLifecycle() throws Exception {
        // List operations
        final Result<Pageable<OperationResponse>> operationResponsePageable = cloudConvertClient.tasks().operations();
        assertThat(operationResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // List tasks
        final Result<Pageable<TaskResponse>> taskResponsePageable = cloudConvertClient.tasks().list(ImmutableMap.of(), ImmutableList.of(), new Pagination(10, 1));
        assertThat(taskResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete tasks
        for (TaskResponse taskResponse : taskResponsePageable.getBody().get().getData()) {
            final Result<Void> waitTaskResponseDataResult = cloudConvertClient.tasks().delete(taskResponse.getId());
            assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
        }
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        cloudConvertClient.close();
    }
}
