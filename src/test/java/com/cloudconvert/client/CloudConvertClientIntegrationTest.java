package com.cloudconvert.client;

import com.cloudconvert.dto.Event;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.*;
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class CloudConvertClientIntegrationTest extends AbstractTest {

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

    private CloudConvertClient cloudConvertClient;

    private InputStream jpgTestFile1InputStream;

    private InputStream jpgTestFile2InputStream;

    private InputStream odtTestFile1InputStream;

    private InputStream odtTestFile2InputStream;

    @Before
    public void before() throws Exception {
        tika = new Tika();
        cloudConvertClient = new CloudConvertClient(true);
        jpgTestFile1InputStream = CloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
        jpgTestFile2InputStream = CloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_2);
        odtTestFile1InputStream = CloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_1);
        odtTestFile2InputStream = CloudConvertClientIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_2);
    }

    @Test(timeout = TIMEOUT)
    public void cancelAndRetryImportUploadTaskLifecycle() throws Exception {
        // Import upload (not immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest());
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(importUploadTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Cancel
        final Result<TaskResponseData> cancelUploadImportTaskResponseDataResult = cloudConvertClient.tasks().cancel(importUploadTaskResponse.getId());
        assertThat(cancelUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse cancelImportUploadTaskResponse = cancelUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(cancelImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(cancelImportUploadTaskResponse.getStatus()).isEqualTo(Status.ERROR);
        assertThat(cancelImportUploadTaskResponse.getCode()).isEqualTo("CANCELLED");
        assertThat(cancelImportUploadTaskResponse.getId()).isEqualTo(importUploadTaskResponse.getId());

        // Retry
        final Result<TaskResponseData> retryUploadImportTaskResponseDataResult = cloudConvertClient.tasks().retry(importUploadTaskResponse.getId());
        assertThat(retryUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse retryImportUploadTaskResponse = retryUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(retryImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(retryImportUploadTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Upload (actual upload)
        final Result<TaskResponseData> uploadTaskResponseDataResult = cloudConvertClient.importUsing().upload(retryUploadImportTaskResponseDataResult, jpgTestFile1InputStream);
        assertThat(uploadTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadTaskResponse = uploadTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait
        final Result<TaskResponseData> waitUploadImportTaskResponseDataResult = cloudConvertClient.tasks().wait(retryImportUploadTaskResponse.getId());
        assertThat(waitUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitImportUploadTaskResponse = waitUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitImportUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitImportUploadTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitImportUploadTaskResponse.getId()).isEqualTo(retryImportUploadTaskResponse.getId());
    }

    @Test(timeout = TIMEOUT)
    public void importUploadAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponseData> waitUploadImportTaskResponseDataResult = cloudConvertClient.tasks().wait(importUploadTaskResponse.getId());
        assertThat(waitUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadImportTaskResponse = waitUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(importUploadTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final Result<TaskResponseData> waitUrlExportTaskResponseDataResult = cloudConvertClient.tasks().wait(urlExportTaskResponse.getId());
        assertThat(waitUrlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadExportTaskResponse = waitUrlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(waitUploadExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUploadExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUploadExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        final Result<InputStream> inputStreamResult = cloudConvertClient.files().download(waitUploadExportTaskResponse.getResult().getFiles().get(0).get("url"));
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody().get())).getName()).isEqualTo("image/jpeg");
    }

    @Test(timeout = TIMEOUT)
    public void convertFileTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
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
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
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
        final Result<JobResponseData> jobResponseDataResult = cloudConvertClient.jobs().create(tasks);
        assertThat(jobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final JobResponse jobResponse = jobResponseDataResult.getBody().get().getData();
        assertThat(jobResponse.getTasks()).hasSize(3);
        assertThat(jobResponse.getStatus()).isEqualTo(Status.WAITING);
        assertThat(jobResponse.getTasks()).extracting(TaskResponse::getName).contains(uploadFile1TaskName, uploadFile2TaskName, mergeFile1AndFile2TaskName);

        final TaskResponse uploadFile1TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(uploadFile1TaskName)).findFirst().get();
        final TaskResponse uploadFile2TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(uploadFile2TaskName)).findFirst().get();
        final TaskResponse mergeFile1AndFile2TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(mergeFile1AndFile2TaskName)).findFirst().get();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds()).isNotEmpty();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds()).contains(uploadFile1TaskJobResponse.getId(), uploadFile2TaskJobResponse.getId());

        // Upload (actual upload file 1)
        final Result<TaskResponseData> uploadFile1TaskResponseDataResult = cloudConvertClient.importUsing()
            .upload(uploadFile1TaskJobResponse.getId(), uploadFile1TaskJobResponse.getResult().getForm(), odtTestFile1InputStream);
        assertThat(uploadFile1TaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile1TaskResponse = uploadFile1TaskResponseDataResult.getBody().get().getData();
        assertThat(uploadFile1TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Upload (actual upload file 2)
        final Result<TaskResponseData> uploadFile2TaskResponseDataResult = cloudConvertClient.importUsing()
            .upload(uploadFile2TaskJobResponse.getId(), uploadFile2TaskJobResponse.getResult().getForm(), odtTestFile2InputStream);
        assertThat(uploadFile2TaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile2TaskResponse = uploadFile2TaskResponseDataResult.getBody().get().getData();
        assertThat(uploadFile2TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait
        final Result<JobResponseData> waitJobResponseDataResult = cloudConvertClient.jobs().wait(jobResponse.getId());
        assertThat(waitJobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse waitJobResponse = waitJobResponseDataResult.getBody().get().getData();
        assertThat(waitJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Show
        final Result<JobResponseData> showJobResponseDataResult = cloudConvertClient.jobs().show(jobResponse.getId());
        assertThat(showJobResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse showJobResponse = showJobResponseDataResult.getBody().get().getData();
        assertThat(showJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Delete job
        final Result<Void> deleteVoidResult = cloudConvertClient.jobs().delete(jobResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void createArchiveTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
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
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
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

    /**
     * As most of the imports are cloud-based, we do not test full lifecycle of all imports, but make sure that import tasks are created
     * The only import, which has the whole lifecycle tested is import with upload
     */
    @Test(timeout = TIMEOUT)
    public void importTasksCreation() throws Exception {
        // Import url
        final UrlImportRequest urlImportRequest = new UrlImportRequest().setUrl("http://some-url.com").setFilename("some-filename.jpg");
        final Result<TaskResponseData> urlImportTaskResponseDataResult = cloudConvertClient.importUsing().url(urlImportRequest);
        assertThat(urlImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlImportTaskResponse = urlImportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_URL);

        // Import upload
        final UploadImportRequest uploadImportRequest = new UploadImportRequest();
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(uploadImportRequest);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Import s3
        final S3ImportRequest s3ImportRequest = new S3ImportRequest().setBucket("some-bucket").setRegion("some-region")
            .setAccessKeyId("some-access-key-id").setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponseData> s3ImportTaskResponseDataResult = cloudConvertClient.importUsing().s3(s3ImportRequest);
        assertThat(s3ImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ImportTaskResponse = s3ImportTaskResponseDataResult.getBody().get().getData();
        assertThat(s3ImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_S3);

        // Import Azure Blob
        final AzureBlobImportRequest azureBlobImportRequest = new AzureBlobImportRequest().setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponseData> azureBlobImportTaskResponseDataResult = cloudConvertClient.importUsing().azureBlob(azureBlobImportRequest);
        assertThat(azureBlobImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobImportTaskResponse = azureBlobImportTaskResponseDataResult.getBody().get().getData();
        assertThat(azureBlobImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_AZURE_BLOB);

        // Import Google Cloud Storage
        final GoogleCloudStorageImportRequest googleCloudStorageImportRequest = new GoogleCloudStorageImportRequest()
            .setProjectId("some-project-id").setBucket("some-bucket").setClientEmail("some-client-email")
            .setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponseData> googleCloudStorageImportTaskResponseDataResult = cloudConvertClient.importUsing().googleCloudStorage(googleCloudStorageImportRequest);
        assertThat(googleCloudStorageImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageImportTaskResponse = googleCloudStorageImportTaskResponseDataResult.getBody().get().getData();
        assertThat(googleCloudStorageImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_GOOGLE_CLOUD_STORAGE);

        // Import Open Stack
        final OpenStackImportRequest openStackImportRequest = new OpenStackImportRequest()
            .setAuthUrl("some-auth-url").setUsername("some-username").setPassword("some-password")
            .setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponseData> openStackImportTaskResponseDataResult = cloudConvertClient.importUsing().openStack(openStackImportRequest);
        assertThat(openStackImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackImportTaskResponse = openStackImportTaskResponseDataResult.getBody().get().getData();
        assertThat(openStackImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_OPENSTACK);

        // Import SFTP
        final SftpImportRequest sftpImportRequest = new SftpImportRequest().setHost("some-host").setUsername("some-username")
            .setPassword("some-password").setFile("some-file");
        final Result<TaskResponseData> sftpImportTaskResponseDataResult = cloudConvertClient.importUsing().sftp(sftpImportRequest);
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
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTestFile1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse importUploadTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(importUploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(importUploadTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Export s3
        final S3ExportRequest s3ExportRequest = new S3ExportRequest().setInput(importUploadTaskResponse.getId())
            .setBucket("some-bucket").setRegion("some-region").setAccessKeyId("some-access-key-id")
            .setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponseData> s3ExportTaskResponseDataResult = cloudConvertClient.exportUsing().s3(s3ExportRequest);
        assertThat(s3ExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ExportTaskResponse = s3ExportTaskResponseDataResult.getBody().get().getData();
        assertThat(s3ExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_S3);

        // Export Azure Blob
        final AzureBlobExportRequest azureBlobExportRequest = new AzureBlobExportRequest()
            .setInput(importUploadTaskResponse.getId()).setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponseData> azureBlobExportTaskResponseDataResult = cloudConvertClient.exportUsing().azureBlob(azureBlobExportRequest);
        assertThat(azureBlobExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobExportTaskResponse = azureBlobExportTaskResponseDataResult.getBody().get().getData();
        assertThat(azureBlobExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_AZURE_BLOB);

        // Export Google Cloud Storage
        final GoogleCloudStorageExportRequest googleCloudStorageExportRequest = new GoogleCloudStorageExportRequest()
            .setInput(importUploadTaskResponse.getId()).setProjectId("some-project-id")
            .setBucket("some-bucket").setClientEmail("some-client-email").setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponseData> googleCloudStorageExportTaskResponseDataResult = cloudConvertClient.exportUsing().googleCloudStorage(googleCloudStorageExportRequest);
        assertThat(googleCloudStorageExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageExportTaskResponse = googleCloudStorageExportTaskResponseDataResult.getBody().get().getData();
        assertThat(googleCloudStorageExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_GOOGLE_CLOUD_STORAGE);

        // Export Open Stack
        final OpenStackExportRequest openStackExportRequest = new OpenStackExportRequest()
            .setInput(importUploadTaskResponse.getId()).setAuthUrl("some-auth-url").setUsername("some-username")
            .setPassword("some-password").setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponseData> openStackExportTaskResponseDataResult = cloudConvertClient.exportUsing().openStack(openStackExportRequest);
        assertThat(openStackExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackExportTaskResponse = openStackExportTaskResponseDataResult.getBody().get().getData();
        assertThat(openStackExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_OPENSTACK);

        // Export SFTP
        final SftpExportRequest sftpExportRequest = new SftpExportRequest().setInput(importUploadTaskResponse.getId())
            .setHost("some-host").setUsername("some-username").setPassword("some-password").setFile("some-file");
        final Result<TaskResponseData> sftpExportTaskResponseDataResult = cloudConvertClient.exportUsing().sftp(sftpExportRequest);
        assertThat(sftpExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpExportTaskResponse = sftpExportTaskResponseDataResult.getBody().get().getData();
        assertThat(sftpExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_SFTP);
    }

    @Test(timeout = TIMEOUT)
    public void listAndDeleteTasksAndJobsLifecycle() throws Exception {
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

        // List jobs
        final Result<Pageable<JobResponse>> jobResponsePageable = cloudConvertClient.jobs().list(ImmutableMap.of(), ImmutableList.of(), new Pagination(10, 1));
        assertThat(jobResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete jobs
        for (JobResponse jobResponse : jobResponsePageable.getBody().get().getData()) {
            final Result<Void> waitTaskResponseDataResult = cloudConvertClient.jobs().delete(jobResponse.getId());
            assertThat(waitTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
        }
    }

    @Test(timeout = TIMEOUT)
    public void userLifecycle() throws Exception {
        final Result<UserResponseData> userResponseDataResult = cloudConvertClient.users().me();
        assertThat(userResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userResponseDataResult.getBody()).get().isNotNull();
    }

    @Test(timeout = TIMEOUT)
    public void webhooksLifecycle() throws Exception {
        // Create
        final WebhookRequest webhookRequest = new WebhookRequest().setUrl("http://some-url.com")
            .setEvents(ImmutableList.of(Event.JOB_CREATED, Event.JOB_FAILED, Event.JOB_FINISHED));
        final Result<WebhookResponseData> createWebhookResponseDataResult = cloudConvertClient.webhooks().create(webhookRequest);
        assertThat(createWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(createWebhookResponseDataResult.getBody()).get().isNotNull();

        final WebhookResponse createWebhookResponse = createWebhookResponseDataResult.getBody().get().getData();
        assertThat(createWebhookResponse.getSigningSecret()).isNotNull();

        // List
        final Result<Pageable<WebhookResponse>> webhookResponsePageable = cloudConvertClient.webhooks().list();
        assertThat(webhookResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);

        // Delete
        final Result<Void> deleteWebhookResponseDataResult = cloudConvertClient.webhooks().delete(createWebhookResponse.getId());
        assertThat(deleteWebhookResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);

        // Verify
        assertThat(cloudConvertClient.webhooks().verify(WEBHOOK_PAYLOAD, WEBHOOK_SIGNATURE)).isTrue();
    }

    @After
    public void after() throws Exception {
        jpgTestFile1InputStream.close();
        jpgTestFile2InputStream.close();
        odtTestFile1InputStream.close();
        odtTestFile2InputStream.close();
        cloudConvertClient.close();
    }
}
