package com.cloudconvert.client;

import com.cloudconvert.dto.Event;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.*;
import com.cloudconvert.dto.result.AsyncResult;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class AsyncCloudConvertClientIntegrationTest extends AbstractTest {

    private static final String JPG = "jpg";
    private static final String PNG = "png";
    private static final String PDF = "pdf";
    private static final String ZIP = "zip";
    private static final String URL = "https://cloudconvert.com/";

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";
    private static final String JPG_TEST_FILE_2 = "image-test-file-2.jpg";

    private static final String ODT_TEST_FILE_1 = "odt-test-file-1.odt";
    private static final String ODT_TEST_FILE_2 = "odt-test-file-2.odt";

    private static final String WEBHOOK_PAYLOAD = "cloudconvert";
    private static final String WEBHOOK_SIGNATURE = "5c4c0691bce8a1a2af738b7073fe0627e792734813358c5f88a658819dd0a6d2";

    private Tika tika;

    private AsyncCloudConvertClient asyncCloudConvertClient;

    private File jpgTest1File;

    private InputStream jpgTest1InputStream;

    private InputStream jpgTest2InputStream;

    private InputStream odtTest1InputStream;

    private InputStream odtTest2InputStream;

    @Before
    public void before() throws Exception {
        tika = new Tika();
        asyncCloudConvertClient = new AsyncCloudConvertClient(true);

        jpgTest1File = new File(CloudConvertClientIntegrationTest.class.getClassLoader().getResource(JPG_TEST_FILE_1).toURI());

        jpgTest1InputStream = AsyncCloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
        jpgTest2InputStream = AsyncCloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_2);
        odtTest1InputStream = AsyncCloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_1);
        odtTest2InputStream = AsyncCloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_2);
    }

    @Test(timeout = TIMEOUT)
    public void cancelAndRetryImportUploadTaskLifecycle() throws Exception {
        // Import upload (not immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest()).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(importUploadTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Cancel
        final Result<TaskResponseData> cancelUploadImportTaskResponseDataResult = asyncCloudConvertClient.tasks().cancel(importUploadTaskResponse.getId()).get();
        assertThat(cancelUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse cancelImportUploadTaskResponse = cancelUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(cancelImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(cancelImportUploadTaskResponse.getStatus()).isEqualTo(Status.ERROR);
        assertThat(cancelImportUploadTaskResponse.getCode()).isEqualTo("CANCELLED");
        assertThat(cancelImportUploadTaskResponse.getId()).isEqualTo(importUploadTaskResponse.getId());

        // Retry
        final AsyncResult<TaskResponseData> retryUploadImportTaskResponseDataAsyncResult = asyncCloudConvertClient.tasks().retry(importUploadTaskResponse.getId());
        final Result<TaskResponseData> retryUploadImportTaskResponseDataResult = retryUploadImportTaskResponseDataAsyncResult.get();

        assertThat(retryUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse retryImportUploadTaskResponse = retryUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(retryImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(retryImportUploadTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Upload (actual upload)
        final Result<TaskResponseData> uploadTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(retryUploadImportTaskResponseDataAsyncResult, jpgTest1InputStream).get();
        assertThat(uploadTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadTaskResponse = uploadTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait
        final Result<TaskResponseData> waitUploadImportTaskResponseDataResult = asyncCloudConvertClient.tasks()
            .wait(retryImportUploadTaskResponse.getId()).get();
        assertThat(waitUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitImportUploadTaskResponse = waitUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitImportUploadTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitImportUploadTaskResponse.getId()).isEqualTo(retryImportUploadTaskResponse.getId());
    }

    @Test(timeout = TIMEOUT)
    public void importUploadInputStreamAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponseData> waitUploadImportTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(importUploadTaskResponse.getId()).get();
        assertThat(waitUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadImportTaskResponse = waitUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(importUploadTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().url(urlExportRequest).get();
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final Result<TaskResponseData> waitUrlExportTaskResponseDataResult = asyncCloudConvertClient.tasks()
            .wait(urlExportTaskResponse.getId()).get();
        assertThat(waitUrlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadExportTaskResponse = waitUrlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUploadExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUploadExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        final Result<InputStream> inputStreamResult = asyncCloudConvertClient.files().download(waitUploadExportTaskResponse.getResult().getFiles().get(0).get("url")).get();
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody().get())).getName()).isEqualTo("image/jpeg");
    }

    @Test(timeout = TIMEOUT)
    public void importUploadFileAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1File).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponseData> waitUploadImportTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(importUploadTaskResponse.getId()).get();
        assertThat(waitUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadImportTaskResponse = waitUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(importUploadTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().url(urlExportRequest).get();
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final Result<TaskResponseData> waitUrlExportTaskResponseDataResult = asyncCloudConvertClient.tasks()
            .wait(urlExportTaskResponse.getId()).get();
        assertThat(waitUrlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadExportTaskResponse = waitUrlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUploadExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUploadExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        final Result<InputStream> inputStreamResult = asyncCloudConvertClient.files().download(waitUploadExportTaskResponse.getResult().getFiles().get(0).get("url")).get();
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody().get())).getName()).isEqualTo("image/jpeg");
    }

    @Test(timeout = TIMEOUT)
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // List convert formats
        final Result<Pageable<OperationResponse>> convertFormatsOperationResponsePageableResult = asyncCloudConvertClient.tasks().convertFormats().get();
        assertThat(convertFormatsOperationResponsePageableResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final List<OperationResponse> operationResponses = convertFormatsOperationResponsePageableResult.getBody().get().getData();
        assertThat(operationResponses).anySatisfy(operationResponse -> assertThat(operationResponse.getOutputFormat()).isEqualTo(JPG));

        // Convert
        final ConvertFilesTaskRequest convertFilesTaskRequest = new ConvertFilesTaskRequest().setInput(importUploadTaskResponse.getId()).setInputFormat(JPG).setOutputFormat(PNG);
        final Result<TaskResponseData> convertTaskResponseDataResult = asyncCloudConvertClient.tasks().convert(convertFilesTaskRequest).get();
        assertThat(convertTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse convertTaskResponse = convertTaskResponseDataResult.getBody().get().getData();
        assertThat(convertTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(convertTaskResponse.getId()).get();
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = asyncCloudConvertClient.tasks().show(convertTaskResponse.getId()).get();
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.CONVERT);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(convertTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(convertTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void optimizeFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Optimize
        final OptimizeFilesTaskRequest optimizeFilesTaskRequest = new OptimizeFilesTaskRequest().setInput(importUploadTaskResponse.getId()).setInputFormat(JPG);
        final Result<TaskResponseData> optimizeTaskResponseDataResult = asyncCloudConvertClient.tasks().optimize(optimizeFilesTaskRequest).get();
        assertThat(optimizeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse optimizeTaskResponse = optimizeTaskResponseDataResult.getBody().get().getData();
        assertThat(optimizeTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(optimizeTaskResponse.getId()).get();
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = asyncCloudConvertClient.tasks().show(optimizeTaskResponse.getId()).get();
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.OPTIMIZE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(optimizeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(optimizeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void captureWebsiteTaskLifecycle() throws Exception {
        // Capture
        final CaptureWebsitesTaskRequest captureWebsitesTaskRequest = new CaptureWebsitesTaskRequest().setUrl(URL).setOutputFormat(PDF);
        final Result<TaskResponseData> captureTaskResponseDataResult = asyncCloudConvertClient.tasks().capture(captureWebsitesTaskRequest).get();
        assertThat(captureTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse captureTaskResponse = captureTaskResponseDataResult.getBody().get().getData();
        assertThat(captureTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(captureTaskResponse.getId()).get();
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = asyncCloudConvertClient.tasks().show(captureTaskResponse.getId()).get();
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.CAPTURE_WEBSITE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(captureTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(captureTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * There are few restrictions from the API:
     * 1. Conversion, which has multiple input files is allowed within jobs only;
     * 2. Merge requires at least 2 input files to be passed
     * <p>
     * So this integration test tests job lifecycle, which contains (upload + upload + merge + export url) tasks then delete the job
     */
    @Test(timeout = TIMEOUT)
    public void mergeFileTaskLifecycle() throws Exception {
        final String uploadFile1TaskName = "import-image-test-file-1";
        final String uploadFile2TaskName = "import-image-test-file-2";
        final String mergeFile1AndFile2TaskName = "image-test-file-1-and-import-image-test-file-2-merge";

        final Map<String, TaskRequest> tasks = ImmutableMap.of(
            uploadFile1TaskName, new UploadImportRequest(),
            uploadFile2TaskName, new UploadImportRequest(),
            mergeFile1AndFile2TaskName, new MergeFilesTaskRequest().setOutputFormat(PDF).setInput("import-image-test-file-1", "import-image-test-file-2")
        );

        // Create job
        final Result<JobResponseData> jobResponseDataResult = asyncCloudConvertClient.jobs().create(tasks).get();
        assertThat(jobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final JobResponse jobResponse = jobResponseDataResult.getBody().get().getData();
        assertThat(jobResponse.getTasks()).hasSize(3);
        assertThat(jobResponse.getStatus()).isEqualTo(Status.WAITING);
        assertThat(jobResponse.getTasks()).extracting(TaskResponse::getName).contains(uploadFile1TaskName, uploadFile2TaskName, mergeFile1AndFile2TaskName);

        final TaskResponse uploadFile1TaskJobResponse = jobResponse.getTasks().stream().filter(taskResponse -> taskResponse.getName()
            .equals(uploadFile1TaskName)).findFirst().get();
        final TaskResponse uploadFile2TaskJobResponse = jobResponse.getTasks().stream().filter(taskResponse -> taskResponse.getName()
            .equals(uploadFile2TaskName)).findFirst().get();
        final TaskResponse mergeFile1AndFile2TaskJobResponse = jobResponse.getTasks().stream().filter(taskResponse -> taskResponse.getName()
            .equals(mergeFile1AndFile2TaskName)).findFirst().get();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds()).isNotEmpty();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds())
            .contains(uploadFile1TaskJobResponse.getId(), uploadFile2TaskJobResponse.getId());

        // Upload (actual upload file 1)
        final Result<TaskResponseData> uploadFile1TaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(uploadFile1TaskJobResponse.getId(), uploadFile1TaskJobResponse.getResult().getForm(), odtTest1InputStream).get();
        assertThat(uploadFile1TaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile1TaskResponse = uploadFile1TaskResponseDataResult.getBody().get().getData();
        assertThat(uploadFile1TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Upload (actual upload file 2)
        final Result<TaskResponseData> uploadFile2TaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(uploadFile2TaskJobResponse.getId(), uploadFile2TaskJobResponse.getResult().getForm(), odtTest2InputStream).get();
        assertThat(uploadFile2TaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile2TaskResponse = uploadFile2TaskResponseDataResult.getBody().get().getData();
        assertThat(uploadFile2TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait
        final Result<JobResponseData> waitJobResponseDataResult = asyncCloudConvertClient.jobs().wait(jobResponse.getId()).get();
        assertThat(waitJobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse waitJobResponse = waitJobResponseDataResult.getBody().get().getData();
        assertThat(waitJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Show
        final Result<JobResponseData> showJobResponseDataResult = asyncCloudConvertClient.jobs().show(jobResponse.getId()).get();
        assertThat(showJobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse showJobResponse = showJobResponseDataResult.getBody().get().getData();
        assertThat(showJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Delete job
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.jobs().delete(jobResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Archive
        final CreateArchivesTaskRequest createArchivesTaskRequest = new CreateArchivesTaskRequest()
            .setInput(importUploadTaskResponse.getId()).setOutputFormat(ZIP);
        final Result<TaskResponseData> archiveTaskResponseDataResult = asyncCloudConvertClient.tasks().archive(createArchivesTaskRequest).get();
        assertThat(archiveTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse archiveTaskResponse = archiveTaskResponseDataResult.getBody().get().getData();
        assertThat(archiveTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(archiveTaskResponse.getId()).get();
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = asyncCloudConvertClient.tasks().show(archiveTaskResponse.getId()).get();
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.ARCHIVE);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(archiveTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(archiveTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void executeCommandTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Command
        final ExecuteCommandsTaskRequest executeCommandsTaskRequest = new ExecuteCommandsTaskRequest()
            .setInput(importUploadTaskResponse.getId()).setEngine(ExecuteCommandsTaskRequest.Engine.GRAPHICSMAGICK)
            .setCommand(ExecuteCommandsTaskRequest.Command.GM).setArguments("version");
        final Result<TaskResponseData> executeTaskResponseDataResult = asyncCloudConvertClient.tasks().command(executeCommandsTaskRequest).get();
        assertThat(executeTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse executeTaskResponse = executeTaskResponseDataResult.getBody().get().getData();
        assertThat(executeTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);

        // Wait
        final Result<TaskResponseData> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().wait(executeTaskResponse.getId()).get();
        assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitTaskResponse = waitTaskResponseDataResult.getBody().get().getData();
        assertThat(waitTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(waitTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Show
        final Result<TaskResponseData> showTaskResponseDataResult = asyncCloudConvertClient.tasks().show(executeTaskResponse.getId()).get();
        assertThat(showTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse showTaskResponse = showTaskResponseDataResult.getBody().get().getData();
        assertThat(showTaskResponse.getOperation()).isEqualTo(Operation.COMMAND);
        assertThat(showTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showTaskResponse.getId()).isEqualTo(executeTaskResponse.getId());

        // Delete
        final Result<Void> deleteVoidResult = asyncCloudConvertClient.tasks().delete(executeTaskResponse.getId()).get();
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * As most of the imports are cloud-based, we do not test full lifecycle of all imports, but make sure that import tasks are created
     * The only import, which has the whole lifecycle tested is import with upload
     */
    @Test(timeout = TIMEOUT)
    public void importTasksCreation() throws Exception {
        // Import url
        final UrlImportRequest urlImportRequest = new UrlImportRequest().setUrl("http://some-url.com").setFilename("some-filename.jpg");
        final Result<TaskResponseData> urlImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().url(urlImportRequest).get();
        assertThat(urlImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlImportTaskResponse = urlImportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_URL);

        // Import upload
        final UploadImportRequest uploadImportRequest = new UploadImportRequest();
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().upload(uploadImportRequest).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Import s3
        final S3ImportRequest s3ImportRequest = new S3ImportRequest().setBucket("some-bucket").setRegion("some-region")
            .setAccessKeyId("some-access-key-id").setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponseData> s3ImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().s3(s3ImportRequest).get();
        assertThat(s3ImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ImportTaskResponse = s3ImportTaskResponseDataResult.getBody().get().getData();
        assertThat(s3ImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_S3);

        // Import Azure Blob
        final AzureBlobImportRequest azureBlobImportRequest = new AzureBlobImportRequest().setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponseData> azureBlobImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().azureBlob(azureBlobImportRequest).get();
        assertThat(azureBlobImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobImportTaskResponse = azureBlobImportTaskResponseDataResult.getBody().get().getData();
        assertThat(azureBlobImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_AZURE_BLOB);

        // Import Google Cloud Storage
        final GoogleCloudStorageImportRequest googleCloudStorageImportRequest = new GoogleCloudStorageImportRequest()
            .setProjectId("some-project-id").setBucket("some-bucket").setClientEmail("some-client-email")
            .setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponseData> googleCloudStorageImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .googleCloudStorage(googleCloudStorageImportRequest).get();
        assertThat(googleCloudStorageImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageImportTaskResponse = googleCloudStorageImportTaskResponseDataResult.getBody().get().getData();
        assertThat(googleCloudStorageImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_GOOGLE_CLOUD_STORAGE);

        // Import Open Stack
        final OpenStackImportRequest openStackImportRequest = new OpenStackImportRequest()
            .setAuthUrl("some-auth-url").setUsername("some-username").setPassword("some-password")
            .setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponseData> openStackImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().openStack(openStackImportRequest).get();
        assertThat(openStackImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackImportTaskResponse = openStackImportTaskResponseDataResult.getBody().get().getData();
        assertThat(openStackImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_OPENSTACK);

        // Import SFTP
        final SftpImportRequest sftpImportRequest = new SftpImportRequest().setHost("some-host").setUsername("some-username")
            .setPassword("some-password").setFile("some-file");
        final Result<TaskResponseData> sftpImportTaskResponseDataResult = asyncCloudConvertClient.importUsing().sftp(sftpImportRequest).get();
        assertThat(sftpImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpImportTaskResponse = sftpImportTaskResponseDataResult.getBody().get().getData();
        assertThat(sftpImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_SFTP);
    }

    /**
     * As most of the exports are cloud-based, we do not test full lifecycle of all exports, but make sure that export tasks are created
     * The only export, which has the whole lifecycle tested is export with url
     */
    @Test(timeout = TIMEOUT)
    public void exportTasksCreation() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = asyncCloudConvertClient.importUsing()
            .upload(new UploadImportRequest(), jpgTest1InputStream).get();
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(importUploadTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().url(urlExportRequest).get();
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Export s3
        final S3ExportRequest s3ExportRequest = new S3ExportRequest().setInput(importUploadTaskResponse.getId())
            .setBucket("some-bucket").setRegion("some-region").setAccessKeyId("some-access-key-id")
            .setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponseData> s3ExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().s3(s3ExportRequest).get();
        assertThat(s3ExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ExportTaskResponse = s3ExportTaskResponseDataResult.getBody().get().getData();
        assertThat(s3ExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_S3);

        // Export Azure Blob
        final AzureBlobExportRequest azureBlobExportRequest = new AzureBlobExportRequest()
            .setInput(importUploadTaskResponse.getId()).setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponseData> azureBlobExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().azureBlob(azureBlobExportRequest).get();
        assertThat(azureBlobExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobExportTaskResponse = azureBlobExportTaskResponseDataResult.getBody().get().getData();
        assertThat(azureBlobExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_AZURE_BLOB);

        // Export Google Cloud Storage
        final GoogleCloudStorageExportRequest googleCloudStorageExportRequest = new GoogleCloudStorageExportRequest()
            .setInput(importUploadTaskResponse.getId()).setProjectId("some-project-id")
            .setBucket("some-bucket").setClientEmail("some-client-email").setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponseData> googleCloudStorageExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing()
            .googleCloudStorage(googleCloudStorageExportRequest).get();
        assertThat(googleCloudStorageExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageExportTaskResponse = googleCloudStorageExportTaskResponseDataResult.getBody().get().getData();
        assertThat(googleCloudStorageExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_GOOGLE_CLOUD_STORAGE);

        // Export Open Stack
        final OpenStackExportRequest openStackExportRequest = new OpenStackExportRequest()
            .setInput(importUploadTaskResponse.getId()).setAuthUrl("some-auth-url").setUsername("some-username")
            .setPassword("some-password").setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponseData> openStackExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().openStack(openStackExportRequest).get();
        assertThat(openStackExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackExportTaskResponse = openStackExportTaskResponseDataResult.getBody().get().getData();
        assertThat(openStackExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_OPENSTACK);

        // Export SFTP
        final SftpExportRequest sftpExportRequest = new SftpExportRequest().setInput(importUploadTaskResponse.getId())
            .setHost("some-host").setUsername("some-username").setPassword("some-password").setFile("some-file");
        final Result<TaskResponseData> sftpExportTaskResponseDataResult = asyncCloudConvertClient.exportUsing().sftp(sftpExportRequest).get();
        assertThat(sftpExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpExportTaskResponse = sftpExportTaskResponseDataResult.getBody().get().getData();
        assertThat(sftpExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_SFTP);
    }

    @Test(timeout = TIMEOUT)
    public void listAndDeleteTasksAndJobsLifecycle() throws Exception {
        // List operations
        final Result<Pageable<OperationResponse>> operationResponsePageable = asyncCloudConvertClient.tasks().operations().get();
        assertThat(operationResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // List tasks
        final Result<Pageable<TaskResponse>> taskResponsePageable = asyncCloudConvertClient.tasks()
            .list(ImmutableMap.of(), ImmutableList.of(), new Pagination(10, 1)).get();
        assertThat(taskResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete tasks
        for (TaskResponse taskResponse : taskResponsePageable.getBody().get().getData()) {
            final Result<Void> waitTaskResponseDataResult = asyncCloudConvertClient.tasks().delete(taskResponse.getId()).get();
            assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
        }

        // List jobs
        final Result<Pageable<JobResponse>> jobResponsePageable = asyncCloudConvertClient.jobs()
            .list(ImmutableMap.of(), ImmutableList.of(), new Pagination(10, 1)).get();
        assertThat(jobResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete jobs
        for (JobResponse jobResponse : jobResponsePageable.getBody().get().getData()) {
            final Result<Void> waitTaskResponseDataResult = asyncCloudConvertClient.jobs().delete(jobResponse.getId()).get();
            assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
        }
    }

    @Test(timeout = TIMEOUT)
    public void userLifecycle() throws Exception {
        final Result<UserResponseData> userResponseDataResult = asyncCloudConvertClient.users().me().get();
        assertThat(userResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseDataResult.getBody()).get().isNotNull();
    }

    @Test(timeout = TIMEOUT)
    public void webhooksLifecycle() throws Exception {
        // Create
        final WebhookRequest webhookRequest = new WebhookRequest().setUrl("http://some-url.com")
            .setEvents(ImmutableList.of(Event.JOB_CREATED, Event.JOB_FAILED, Event.JOB_FINISHED));
        final Result<WebhookResponseData> createWebhookResponseDataResult = asyncCloudConvertClient.webhooks().create(webhookRequest).get();
        assertThat(createWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(createWebhookResponseDataResult.getBody()).get().isNotNull();

        final WebhookResponse createWebhookResponse = createWebhookResponseDataResult.getBody().get().getData();
        assertThat(createWebhookResponse.getSigningSecret()).isNotNull();

        // List
        final Result<Pageable<WebhookResponse>> webhookResponsePageable = asyncCloudConvertClient.webhooks().list().get();
        assertThat(webhookResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete
        final Result<Void> deleteWebhookResponseDataResult = asyncCloudConvertClient.webhooks().delete(createWebhookResponse.getId()).get();
        assertThat(deleteWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);

        // Verify
        assertThat(asyncCloudConvertClient.webhooks().verify(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE)).isTrue();
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        jpgTest2InputStream.close();
        odtTest1InputStream.close();
        odtTest2InputStream.close();
        asyncCloudConvertClient.close();
    }
}
