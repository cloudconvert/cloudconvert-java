package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
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

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class ImportsAndExportsIntegrationTest extends AbstractTest {

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private Tika tika;

    private CloudConvertClient cloudConvertClient;

    private File jpgTest1File;

    private InputStream jpgTest1InputStream;

    @Before
    public void before() throws Exception {
        tika = new Tika();
        cloudConvertClient = new CloudConvertClient();

        jpgTest1File = new File(ImportsAndExportsIntegrationTest.class.getClassLoader().getResource(JPG_TEST_FILE_1).toURI());

        jpgTest1InputStream = ImportsAndExportsIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);
    }

    @Test(timeout = TIMEOUT)
    public void cancelAndRetryUploadImportTaskLifecycle() throws Exception {
        // Import upload (not immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest());
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(uploadImportTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Cancel
        final Result<TaskResponse> cancelUploadImportTaskResponseResult = cloudConvertClient.tasks().cancel(uploadImportTaskResponse.getId());
        assertThat(cancelUploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse cancelUploadImportTaskResponse = cancelUploadImportTaskResponseResult.getBody();
        assertThat(cancelUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(cancelUploadImportTaskResponse.getStatus()).isEqualTo(Status.ERROR);
        assertThat(cancelUploadImportTaskResponse.getCode()).isEqualTo("CANCELLED");
        assertThat(cancelUploadImportTaskResponse.getId()).isEqualTo(uploadImportTaskResponse.getId());

        // Retry
        final Result<TaskResponse> retryUploadImportTaskResponseResult = cloudConvertClient.tasks().retry(uploadImportTaskResponse.getId());
        assertThat(retryUploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse retryUploadImportTaskResponse = retryUploadImportTaskResponseResult.getBody();
        assertThat(retryUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(retryUploadImportTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Upload (actual upload)
        final Result<TaskResponse> uploadTaskResponseResult = cloudConvertClient.importUsing().upload(retryUploadImportTaskResponseResult, jpgTest1InputStream);
        assertThat(uploadTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadTaskResponse = uploadTaskResponseResult.getBody();
        assertThat(uploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponse> waitRetryUploadImportTaskResponseResult = cloudConvertClient.tasks().wait(retryUploadImportTaskResponse.getId());
        assertThat(waitRetryUploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitRetryUploadImportTaskResponse = waitRetryUploadImportTaskResponseResult.getBody();
        assertThat(waitRetryUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitRetryUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitRetryUploadImportTaskResponse.getId()).isEqualTo(retryUploadImportTaskResponse.getId());
    }

    @Test(timeout = TIMEOUT)
    public void uploadImportInputStreamAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponse> waitUploadImportTaskResponseResult = cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId());
        assertThat(waitUploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadImportTaskResponse = waitUploadImportTaskResponseResult.getBody();
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponse> urlExportTaskResponseResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseResult.getBody();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final Result<TaskResponse> waitUrlExportTaskResponseResult = cloudConvertClient.tasks().wait(urlExportTaskResponse.getId());
        assertThat(waitUrlExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUrlExportTaskResponse = waitUrlExportTaskResponseResult.getBody();
        assertThat(waitUrlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUrlExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUrlExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        // Download file
        final Result<InputStream> inputStreamResult = cloudConvertClient.files().download(waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url"));
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody())).getName()).isEqualTo("image/jpeg");
    }

    @Test(timeout = TIMEOUT)
    public void uploadImportFileAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1File);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final Result<TaskResponse> waitUploadImportTaskResponseResult = cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId());
        assertThat(waitUploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUploadImportTaskResponse = waitUploadImportTaskResponseResult.getBody();
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponse> urlExportTaskResponseResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseResult.getBody();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final Result<TaskResponse> waitUrlExportTaskResponseResult = cloudConvertClient.tasks().wait(urlExportTaskResponse.getId());
        assertThat(waitUrlExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse waitUrlExportTaskResponse = waitUrlExportTaskResponseResult.getBody();
        assertThat(waitUrlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUrlExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUrlExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        // Donload file
        final Result<InputStream> inputStreamResult = cloudConvertClient.files().download(waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url"));
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody())).getName()).isEqualTo("image/jpeg");
    }

    /**
     * As most of the imports are cloud-based, we do not test full lifecycle of all imports, but make sure that import tasks are created
     * The only import, which has the whole lifecycle tested is import with upload
     */
    @Test(timeout = TIMEOUT)
    public void importTasksCreation() throws Exception {
        // Import url
        final UrlImportRequest urlImportRequest = new UrlImportRequest().setUrl("http://some-url.com").setFilename("some-filename.jpg");
        final Result<TaskResponse> urlImportTaskResponseResult = cloudConvertClient.importUsing().url(urlImportRequest);
        assertThat(urlImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlImportTaskResponse = urlImportTaskResponseResult.getBody();
        assertThat(urlImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_URL);

        // Import upload
        final UploadImportRequest uploadImportRequest = new UploadImportRequest();
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(uploadImportRequest);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Import s3
        final S3ImportRequest s3ImportRequest = new S3ImportRequest().setBucket("some-bucket").setRegion("some-region")
            .setAccessKeyId("some-access-key-id").setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponse> s3ImportTaskResponseResult = cloudConvertClient.importUsing().s3(s3ImportRequest);
        assertThat(s3ImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ImportTaskResponse = s3ImportTaskResponseResult.getBody();
        assertThat(s3ImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_S3);

        // Import Azure Blob
        final AzureBlobImportRequest azureBlobImportRequest = new AzureBlobImportRequest().setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponse> azureBlobImportTaskResponseResult = cloudConvertClient.importUsing().azureBlob(azureBlobImportRequest);
        assertThat(azureBlobImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobImportTaskResponse = azureBlobImportTaskResponseResult.getBody();
        assertThat(azureBlobImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_AZURE_BLOB);

        // Import Google Cloud Storage
        final GoogleCloudStorageImportRequest googleCloudStorageImportRequest = new GoogleCloudStorageImportRequest()
            .setProjectId("some-project-id").setBucket("some-bucket").setClientEmail("some-client-email")
            .setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponse> googleCloudStorageImportTaskResponseResult = cloudConvertClient.importUsing().googleCloudStorage(googleCloudStorageImportRequest);
        assertThat(googleCloudStorageImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageImportTaskResponse = googleCloudStorageImportTaskResponseResult.getBody();
        assertThat(googleCloudStorageImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_GOOGLE_CLOUD_STORAGE);

        // Import Open Stack
        final OpenStackImportRequest openStackImportRequest = new OpenStackImportRequest()
            .setAuthUrl("some-auth-url").setUsername("some-username").setPassword("some-password")
            .setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponse> openStackImportTaskResponseResult = cloudConvertClient.importUsing().openStack(openStackImportRequest);
        assertThat(openStackImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackImportTaskResponse = openStackImportTaskResponseResult.getBody();
        assertThat(openStackImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_OPENSTACK);

        // Import SFTP
        final SftpImportRequest sftpImportRequest = new SftpImportRequest().setHost("some-host").setUsername("some-username")
            .setPassword("some-password").setFile("some-file");
        final Result<TaskResponse> sftpImportTaskResponseResult = cloudConvertClient.importUsing().sftp(sftpImportRequest);
        assertThat(sftpImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpImportTaskResponse = sftpImportTaskResponseResult.getBody();
        assertThat(sftpImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_SFTP);

        // Import Base64
        final Base64ImportRequest base64ImportRequest = new Base64ImportRequest().setFile("dGVzdDEyMw==").setFilename("test.txt");
        final Result<TaskResponse> base64ImportTaskResponseResult = cloudConvertClient.importUsing().base64(base64ImportRequest);
        assertThat(base64ImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse base64ImportTaskResponse = base64ImportTaskResponseResult.getBody();
        assertThat(base64ImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_BASE64);

        // Import Raw
        final RawImportRequest rawImportRequest = new RawImportRequest().setFile("content").setFilename("test.txt");
        final Result<TaskResponse> rawImportTaskResponseResult = cloudConvertClient.importUsing().raw(rawImportRequest);
        assertThat(rawImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse rawImportTaskResponse = rawImportTaskResponseResult.getBody();
        assertThat(rawImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_RAW);

    }

    /**
     * As most of the exports are cloud-based, we do not test full lifecycle of all exports, but make sure that export tasks are created
     * The only export, which has the whole lifecycle tested is export with url
     */
    @Test(timeout = TIMEOUT)
    public void exportTasksCreation() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponse> uploadImportTaskResponseResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseResult.getBody();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponse> urlExportTaskResponseResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseResult.getBody();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Export s3
        final S3ExportRequest s3ExportRequest = new S3ExportRequest().setInput(uploadImportTaskResponse.getId())
            .setBucket("some-bucket").setRegion("some-region").setAccessKeyId("some-access-key-id")
            .setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponse> s3ExportTaskResponseResult = cloudConvertClient.exportUsing().s3(s3ExportRequest);
        assertThat(s3ExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ExportTaskResponse = s3ExportTaskResponseResult.getBody();
        assertThat(s3ExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_S3);

        // Export Azure Blob
        final AzureBlobExportRequest azureBlobExportRequest = new AzureBlobExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponse> azureBlobExportTaskResponseResult = cloudConvertClient.exportUsing().azureBlob(azureBlobExportRequest);
        assertThat(azureBlobExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobExportTaskResponse = azureBlobExportTaskResponseResult.getBody();
        assertThat(azureBlobExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_AZURE_BLOB);

        // Export Google Cloud Storage
        final GoogleCloudStorageExportRequest googleCloudStorageExportRequest = new GoogleCloudStorageExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setProjectId("some-project-id")
            .setBucket("some-bucket").setClientEmail("some-client-email").setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponse> googleCloudStorageExportTaskResponseResult = cloudConvertClient.exportUsing().googleCloudStorage(googleCloudStorageExportRequest);
        assertThat(googleCloudStorageExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageExportTaskResponse = googleCloudStorageExportTaskResponseResult.getBody();
        assertThat(googleCloudStorageExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_GOOGLE_CLOUD_STORAGE);

        // Export Open Stack
        final OpenStackExportRequest openStackExportRequest = new OpenStackExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setAuthUrl("some-auth-url").setUsername("some-username")
            .setPassword("some-password").setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponse> openStackExportTaskResponseResult = cloudConvertClient.exportUsing().openStack(openStackExportRequest);
        assertThat(openStackExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackExportTaskResponse = openStackExportTaskResponseResult.getBody();
        assertThat(openStackExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_OPENSTACK);

        // Export SFTP
        final SftpExportRequest sftpExportRequest = new SftpExportRequest().setInput(uploadImportTaskResponse.getId())
            .setHost("some-host").setUsername("some-username").setPassword("some-password").setFile("some-file");
        final Result<TaskResponse> sftpExportTaskResponseResult = cloudConvertClient.exportUsing().sftp(sftpExportRequest);
        assertThat(sftpExportTaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpExportTaskResponse = sftpExportTaskResponseResult.getBody();
        assertThat(sftpExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_SFTP);
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        cloudConvertClient.close();
    }
}
