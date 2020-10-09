package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.OperationResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
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
        cloudConvertClient = new CloudConvertClient();

        jpgTest1InputStream = TasksIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
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
        final Result<TaskResponse> waitConvertTaskResponseResult = cloudConvertClient.tasks().wait(convertTaskResponse.getId());
        assertThat(waitConvertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitConvertTaskResponse = waitConvertTaskResponseResult.getBody();
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

    @Test(timeout = TIMEOUT)
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
        final Result<TaskResponse> waitOptimizeTaskResponseResult = cloudConvertClient.tasks().wait(optimizeTaskResponse.getId());
        assertThat(waitOptimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitOptimizeTaskResponse = waitOptimizeTaskResponseResult.getBody();
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

    @Test(timeout = TIMEOUT)
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponse> captureTaskResponseResult = cloudConvertClient.tasks().capture(captureWebsitesTaskRequest);
        assertThat(captureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseResult.getBody();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final Result<TaskResponse> waitCaptureTaskResponseResult = cloudConvertClient.tasks().wait(captureTaskResponse.getId());
        assertThat(waitCaptureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitCaptureTaskResponse = waitCaptureTaskResponseResult.getBody();
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

    @Test(timeout = TIMEOUT)
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
        final Result<TaskResponse> waitArchiveTaskResponseResult = cloudConvertClient.tasks().wait(archiveTaskResponse.getId());
        assertThat(waitArchiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitArchiveTaskResponse = waitArchiveTaskResponseResult.getBody();
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

    @Test(timeout = TIMEOUT)
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
        final Result<TaskResponse> waitExecuteTaskResponseResult = cloudConvertClient.tasks().wait(executeTaskResponse.getId());
        assertThat(waitExecuteTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitExecuteTaskResponse = waitExecuteTaskResponseResult.getBody();
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

    @Test(timeout = TIMEOUT)
    public void thumbnailFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Thumbnail
        final CreateThumbnailsTaskRequest createThumbnailsTaskRequest = new CreateThumbnailsTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponse> thumbnailTaskResponseResult = cloudConvertClient.tasks().thumbnail(createThumbnailsTaskRequest);
        assertThat(thumbnailTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse thumbnailTaskResponse = thumbnailTaskResponseResult.getBody();
        assertThat(thumbnailTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);

        // Wait
        final Result<TaskResponse> waitThumbnailTaskResponseResult = cloudConvertClient.tasks().wait(thumbnailTaskResponse.getId());
        assertThat(waitThumbnailTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitThumbnailTaskResponse = waitThumbnailTaskResponseResult.getBody();
        assertThat(waitThumbnailTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);
        assertThat(waitThumbnailTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitThumbnailTaskResponse.getId()).isEqualTo(thumbnailTaskResponse.getId());

        // Show
        final Result<TaskResponse> showThumbnailTaskResponseResult = cloudConvertClient.tasks().show(thumbnailTaskResponse.getId());
        assertThat(showThumbnailTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showThumbnailTaskResponse = showThumbnailTaskResponseResult.getBody();
        assertThat(showThumbnailTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);
        assertThat(showThumbnailTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showThumbnailTaskResponse.getId()).isEqualTo(thumbnailTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(thumbnailTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }


    @Test(timeout = TIMEOUT)
    public void getMetadataTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Thumbnail
        final GetMetadataTaskRequest getMetadataTaskRequest = new GetMetadataTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponse> metadataTaskResponseResult = cloudConvertClient.tasks().metadata(getMetadataTaskRequest);
        assertThat(metadataTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse metadataTaskResponse = metadataTaskResponseResult.getBody();
        assertThat(metadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);

        // Wait
        final Result<TaskResponse> waitMetadataTaskResponseResult = cloudConvertClient.tasks().wait(metadataTaskResponse.getId());
        assertThat(waitMetadataTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitMetadataTaskResponse = waitMetadataTaskResponseResult.getBody();
        assertThat(waitMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);
        assertThat(waitMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitMetadataTaskResponse.getId()).isEqualTo(metadataTaskResponse.getId());

        // Show
        final Result<TaskResponse> showMetadataTaskResponseResult = cloudConvertClient.tasks().show(metadataTaskResponse.getId());
        assertThat(showMetadataTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showMetadataTaskResponse = showMetadataTaskResponseResult.getBody();
        assertThat(showMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);
        assertThat(showMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showMetadataTaskResponse.getId()).isEqualTo(metadataTaskResponse.getId());
        assertThat(showMetadataTaskResponse.getResult().getMetadata()).isNotEmpty();

        // Delete
        final Result<Void> deleteVoidResult = cloudConvertClient.tasks().delete(metadataTaskResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }


    @Test(timeout = TIMEOUT)
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
