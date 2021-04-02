package com.cloudconvert.test.integration;

import com.cloudconvert.client.AsyncCloudConvertClient;
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
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Before
    public void before() throws Exception {
        asyncCloudConvertClient = new AsyncCloudConvertClient();

        jpgTest1InputStream = AsyncTasksIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // List convert formats
        final Result<Pageable<OperationResponse>> convertFormatsOperationResponsePageableResult = asyncCloudConvertClient.tasks().convertFormats().get();
        assertThat(convertFormatsOperationResponsePageableResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final List<OperationResponse> operationResponses = convertFormatsOperationResponsePageableResult.getBody().getData();
        assertThat(operationResponses).anySatisfy(operationResponse -> assertThat(operationResponse.getOutputFormat()).isEqualTo(JPG));

        // Convert
        final ConvertFilesTaskRequest convertFilesTaskRequest = new ConvertFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponse> convertTaskResponseResult = asyncCloudConvertClient.tasks().convert(convertFilesTaskRequest).get();
        assertThat(convertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse convertTaskResponse = convertTaskResponseResult.getBody();
        assertThat(convertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);

        // Wait
        final Result<TaskResponse> waitConvertTaskResponseResult = asyncCloudConvertClient.tasks().wait(convertTaskResponse.getId()).get();
        assertThat(waitConvertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitConvertTaskResponse = waitConvertTaskResponseResult.getBody();
        assertThat(waitConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(waitConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Show
        final Result<TaskResponse> showConvertTaskResponseResult = asyncCloudConvertClient.tasks().show(convertTaskResponse.getId()).get();
        assertThat(showConvertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showConvertTaskResponse = showConvertTaskResponseResult.getBody();
        assertThat(showConvertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(showConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showConvertTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(convertTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void optimizeFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Optimize
        final OptimizeFilesTaskRequest optimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponse> optimizeTaskResponseResult = asyncCloudConvertClient.tasks().optimize(optimizeFilesTaskRequest).get();
        assertThat(optimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse optimizeTaskResponse = optimizeTaskResponseResult.getBody();
        assertThat(optimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);

        // Wait
        final Result<TaskResponse> waitOptimizeTaskResponseResult = asyncCloudConvertClient.tasks().wait(optimizeTaskResponse.getId()).get();
        assertThat(waitOptimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitOptimizeTaskResponse = waitOptimizeTaskResponseResult.getBody();
        assertThat(waitOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(waitOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Show
        final Result<TaskResponse> showOptimizeTaskResponseResult = asyncCloudConvertClient.tasks().show(optimizeTaskResponse.getId()).get();
        assertThat(showOptimizeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showOptimizeTaskResponse = showOptimizeTaskResponseResult.getBody();
        assertThat(showOptimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(showOptimizeTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showOptimizeTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(optimizeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponse> captureTaskResponseResult = asyncCloudConvertClient.tasks().capture(captureWebsitesTaskRequest).get();
        assertThat(captureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseResult.getBody();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final Result<TaskResponse> waitCaptureTaskResponseResult = asyncCloudConvertClient.tasks().wait(captureTaskResponse.getId()).get();
        assertThat(waitCaptureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitCaptureTaskResponse = waitCaptureTaskResponseResult.getBody();
        assertThat(waitCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(waitCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Show
        final Result<TaskResponse> showCaptureTaskResponseResult = asyncCloudConvertClient.tasks().show(captureTaskResponse.getId()).get();
        assertThat(showCaptureTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showCaptureTaskResponse = showCaptureTaskResponseResult.getBody();
        assertThat(showCaptureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(showCaptureTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showCaptureTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(captureTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Archive
        final CreateArchivesTaskRequest createArchivesTaskRequest = new CreateArchivesTaskRequest()
            .setInput(uploadImportTaskResponse.getId()).setOutputFormat(ZIP);
        final Result<TaskResponse> archiveTaskResponseResult = asyncCloudConvertClient.tasks().archive(createArchivesTaskRequest).get();
        assertThat(archiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse archiveTaskResponse = archiveTaskResponseResult.getBody();
        assertThat(archiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);

        // Wait
        final Result<TaskResponse> waitArchiveTaskResponseResult = asyncCloudConvertClient.tasks().wait(archiveTaskResponse.getId()).get();
        assertThat(waitArchiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitArchiveTaskResponse = waitArchiveTaskResponseResult.getBody();
        assertThat(waitArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(waitArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Show
        final Result<TaskResponse> showArchiveTaskResponseResult = asyncCloudConvertClient.tasks().show(archiveTaskResponse.getId()).get();
        assertThat(showArchiveTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showArchiveTaskResponse = showArchiveTaskResponseResult.getBody();
        assertThat(showArchiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(showArchiveTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showArchiveTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(archiveTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void executeCommandTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Command
        final ExecuteCommandsTaskRequest executeCommandsTaskRequest = new ExecuteCommandsTaskRequest()
            .setInput(uploadImportTaskResponse.getId()).setEngine(ExecuteCommandsTaskRequest.Engine.GRAPHICSMAGICK)
            .setCommand(ExecuteCommandsTaskRequest.Command.GM).setArguments("version");
        final Result<TaskResponse> executeTaskResponseResult = asyncCloudConvertClient.tasks().command(executeCommandsTaskRequest).get();
        assertThat(executeTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse executeTaskResponse = executeTaskResponseResult.getBody();
        assertThat(executeTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);

        // Wait
        final Result<TaskResponse> waitExecuteTaskResponseResult = asyncCloudConvertClient.tasks().wait(executeTaskResponse.getId()).get();
        assertThat(waitExecuteTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitExecuteTaskResponse = waitExecuteTaskResponseResult.getBody();
        assertThat(waitExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(waitExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Show
        final Result<TaskResponse> showExecuteTaskResponseResult = asyncCloudConvertClient.tasks().show(executeTaskResponse.getId()).get();
        assertThat(showExecuteTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showExecuteTaskResponse = showExecuteTaskResponseResult.getBody();
        assertThat(showExecuteTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(showExecuteTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showExecuteTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(executeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void createThumbnailTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Convert
        final CreateThumbnailsTaskRequest createThumbnailsTaskRequest = new CreateThumbnailsTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponse> thumbnailTaskResponseResult = asyncCloudConvertClient.tasks().thumbnail(createThumbnailsTaskRequest).get();
        assertThat(thumbnailTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse thumbnailTaskResponse = thumbnailTaskResponseResult.getBody();
        assertThat(thumbnailTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);

        // Wait
        final Result<TaskResponse> waitThumbnailTaskResponseResult = asyncCloudConvertClient.tasks().wait(thumbnailTaskResponse.getId()).get();
        assertThat(waitThumbnailTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitThumbnailTaskResponse = waitThumbnailTaskResponseResult.getBody();
        assertThat(waitThumbnailTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);
        assertThat(waitThumbnailTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitThumbnailTaskResponse.getId()).isEqualTo(thumbnailTaskResponse.getId());

        // Show
        final Result<TaskResponse> showConvertTaskResponseResult = asyncCloudConvertClient.tasks().show(thumbnailTaskResponse.getId()).get();
        assertThat(showConvertTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showConvertTaskResponse = showConvertTaskResponseResult.getBody();
        assertThat(showConvertTaskResponse.getOperation()).isEqualTo(Operation.THUMBNAIL);
        assertThat(showConvertTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showConvertTaskResponse.getId()).isEqualTo(thumbnailTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(thumbnailTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void getMetadataTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Get Metadata
        final GetMetadataTaskRequest getMetadataTaskRequest = new GetMetadataTaskRequest().setInput(uploadImportTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponse> getMetadataTaskResponseResult = asyncCloudConvertClient.tasks().metadata(getMetadataTaskRequest).get();
        assertThat(getMetadataTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse metadataTaskResponse = getMetadataTaskResponseResult.getBody();
        assertThat(metadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);

        // Wait
        final Result<TaskResponse> waitGetMetadataResponseResult = asyncCloudConvertClient.tasks().wait(metadataTaskResponse.getId()).get();
        assertThat(waitGetMetadataResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitMetadataTaskResponse = waitGetMetadataResponseResult.getBody();
        assertThat(waitMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);
        assertThat(waitMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitMetadataTaskResponse.getId()).isEqualTo(metadataTaskResponse.getId());

        // Show
        final Result<TaskResponse> showMetadataResponseResult = asyncCloudConvertClient.tasks().show(metadataTaskResponse.getId()).get();
        assertThat(showMetadataResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showMetadataTaskResponse = showMetadataResponseResult.getBody();
        assertThat(showMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA);
        assertThat(showMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showMetadataTaskResponse.getId()).isEqualTo(metadataTaskResponse.getId());
        assertThat(showMetadataTaskResponse.getResult().getMetadata()).isNotEmpty();

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(metadataTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void writeMetadataTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Write Metadata
        final WriteMetadataTaskRequest writeMetadataTaskRequest = new WriteMetadataTaskRequest()
                .setInput(uploadImportTaskResponse.getId())
                .setInputFormat(JPG)
                .setMetadata(new HashMap<String, String>() {{
                    put("Author", "CloudConvert");
                }});
        final Result<TaskResponse> writeMetadataTaskResponseResult = asyncCloudConvertClient.tasks().writeMetadata(writeMetadataTaskRequest).get();
        assertThat(writeMetadataTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse writeMetadataTaskResponse = writeMetadataTaskResponseResult.getBody();
        assertThat(writeMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA_WRITE);

        // Wait
        final Result<TaskResponse> waitWriteMetadataResponseResult = asyncCloudConvertClient.tasks().wait(writeMetadataTaskResponse.getId()).get();
        assertThat(waitWriteMetadataResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitWriteMetadataTaskResponse = waitWriteMetadataResponseResult.getBody();
        assertThat(waitWriteMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA_WRITE);
        assertThat(waitWriteMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitWriteMetadataTaskResponse.getId()).isEqualTo(writeMetadataTaskResponse.getId());

        // Show
        final Result<TaskResponse> showWriteMetadataResponseResult = asyncCloudConvertClient.tasks().show(writeMetadataTaskResponse.getId()).get();
        assertThat(showWriteMetadataResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showWriteMetadataTaskResponse = showWriteMetadataResponseResult.getBody();
        assertThat(showWriteMetadataTaskResponse.getOperation()).isEqualTo(Operation.METADATA_WRITE);
        assertThat(showWriteMetadataTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showWriteMetadataTaskResponse.getId()).isEqualTo(writeMetadataTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(writeMetadataTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }


    @Test(timeout = TIMEOUT)
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
