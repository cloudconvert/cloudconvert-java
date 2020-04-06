package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.AzureBlobExportRequest;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageExportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.OpenStackExportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.S3ExportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpExportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.cloudconvert.test.framework.WaitConditionFactoryProvider;
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
import static org.awaitility.Awaitility.await;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class ImportsAndExportsIntegrationTest extends AbstractTest {

    private static final String JPG_TEST_FILE_1 = "image-test-file-1.jpg";

    private Tika tika;

    private CloudConvertClient cloudConvertClient;

    private File jpgTest1File;

    private InputStream jpgTest1InputStream;

    private WaitConditionFactoryProvider waitConditionFactoryProvider;

    @Before
    public void before() throws Exception {
        tika = new Tika();
        cloudConvertClient = new CloudConvertClient();

        jpgTest1File = new File(ImportsAndExportsIntegrationTest.class.getClassLoader().getResource(JPG_TEST_FILE_1).toURI());

        jpgTest1InputStream = ImportsAndExportsIntegrationTest.class.getClassLoader().getResourceAsStream(JPG_TEST_FILE_1);

        waitConditionFactoryProvider = new WaitConditionFactoryProvider();
    }

    @Test
    public void cancelAndRetryUploadImportTaskLifecycle() throws Exception {
        // Import upload (not immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest());
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(uploadImportTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Cancel
        final Result<TaskResponseData> cancelUploadImportTaskResponseDataResult = cloudConvertClient.tasks().cancel(uploadImportTaskResponse.getId());
        assertThat(cancelUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse cancelUploadImportTaskResponse = cancelUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(cancelUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(cancelUploadImportTaskResponse.getStatus()).isEqualTo(Status.ERROR);
        assertThat(cancelUploadImportTaskResponse.getCode()).isEqualTo("CANCELLED");
        assertThat(cancelUploadImportTaskResponse.getId()).isEqualTo(uploadImportTaskResponse.getId());

        // Retry
        final Result<TaskResponseData> retryUploadImportTaskResponseDataResult = cloudConvertClient.tasks().retry(uploadImportTaskResponse.getId());
        assertThat(retryUploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse retryUploadImportTaskResponse = retryUploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(retryUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(retryUploadImportTaskResponse.getStatus()).isEqualTo(Status.WAITING);

        // Upload (actual upload)
        final Result<TaskResponseData> uploadTaskResponseDataResult = cloudConvertClient.importUsing().upload(retryUploadImportTaskResponseDataResult, jpgTest1InputStream);
        assertThat(uploadTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadTaskResponse = uploadTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final TaskResponse waitRetryUploadImportTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(retryUploadImportTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(retryUploadImportTaskResponse.getId()),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitRetryUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitRetryUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitRetryUploadImportTaskResponse.getId()).isEqualTo(retryUploadImportTaskResponse.getId());
    }

    @Test
    public void uploadImportInputStreamAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final TaskResponse waitUploadImportTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(uploadImportTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final TaskResponse waitUrlExportTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(urlExportTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(urlExportTaskResponse.getId()),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitUrlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUrlExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUrlExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        // Download file
        final Result<InputStream> inputStreamResult = cloudConvertClient.files().download(waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url"));
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody().get())).getName()).isEqualTo("image/jpeg");
    }

    @Test
    public void uploadImportFileAndExportUrlTaskLifecycle() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1File);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait import upload
        final TaskResponse waitUploadImportTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(uploadImportTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitUploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);
        assertThat(waitUploadImportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Wait export url
        final TaskResponse waitUrlExportTaskResponse = await().atMost(AT_MOST).until(() ->
                waitConditionFactoryProvider.provide(urlExportTaskResponse.getId()).until(
                    () -> cloudConvertClient.tasks().wait(urlExportTaskResponse.getId()),
                    awaitTaskResponseDataResult -> awaitTaskResponseDataResult.getStatus() == HttpStatus.SC_OK
                ).getBody().get().getData(),
            waitTaskResponse -> waitTaskResponse.getStatus() == Status.FINISHED
        );
        assertThat(waitUrlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);
        assertThat(waitUrlExportTaskResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitUrlExportTaskResponse.getResult().getFiles()).hasSize(1).hasOnlyOneElementSatisfying(map -> assertThat(map.get("url")).isNotNull());

        // Donload file
        final Result<InputStream> inputStreamResult = cloudConvertClient.files().download(waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url"));
        assertThat(inputStreamResult.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStreamResult.getBody().get())).getName()).isEqualTo("image/jpeg");
    }

    /**
     * As most of the imports are cloud-based, we do not test full lifecycle of all imports, but make sure that import tasks are created
     * The only import, which has the whole lifecycle tested is import with upload
     */
    @Test
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
    @Test
    public void exportTasksCreation() throws Exception {
        // Import upload (immediate upload)
        final Result<TaskResponseData> uploadImportTaskResponseDataResult = cloudConvertClient.importUsing().upload(new UploadImportRequest(), jpgTest1InputStream);
        assertThat(uploadImportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadImportTaskResponse = uploadImportTaskResponseDataResult.getBody().get().getData();
        assertThat(uploadImportTaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Export url
        final UrlExportRequest urlExportRequest = new UrlExportRequest().setInput(uploadImportTaskResponse.getId());
        final Result<TaskResponseData> urlExportTaskResponseDataResult = cloudConvertClient.exportUsing().url(urlExportRequest);
        assertThat(urlExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse urlExportTaskResponse = urlExportTaskResponseDataResult.getBody().get().getData();
        assertThat(urlExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_URL);

        // Export s3
        final S3ExportRequest s3ExportRequest = new S3ExportRequest().setInput(uploadImportTaskResponse.getId())
            .setBucket("some-bucket").setRegion("some-region").setAccessKeyId("some-access-key-id")
            .setSecretAccessKey("some-secret-access-key").setKey("some-key");
        final Result<TaskResponseData> s3ExportTaskResponseDataResult = cloudConvertClient.exportUsing().s3(s3ExportRequest);
        assertThat(s3ExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse s3ExportTaskResponse = s3ExportTaskResponseDataResult.getBody().get().getData();
        assertThat(s3ExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_S3);

        // Export Azure Blob
        final AzureBlobExportRequest azureBlobExportRequest = new AzureBlobExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setStorageAccount("some-storage-account")
            .setStorageAccessKey("some-storage-access-key").setContainer("some-container").setBlob("some-blob");
        final Result<TaskResponseData> azureBlobExportTaskResponseDataResult = cloudConvertClient.exportUsing().azureBlob(azureBlobExportRequest);
        assertThat(azureBlobExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse azureBlobExportTaskResponse = azureBlobExportTaskResponseDataResult.getBody().get().getData();
        assertThat(azureBlobExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_AZURE_BLOB);

        // Export Google Cloud Storage
        final GoogleCloudStorageExportRequest googleCloudStorageExportRequest = new GoogleCloudStorageExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setProjectId("some-project-id")
            .setBucket("some-bucket").setClientEmail("some-client-email").setPrivateKey("some-private-key").setFile("some-file");
        final Result<TaskResponseData> googleCloudStorageExportTaskResponseDataResult = cloudConvertClient.exportUsing().googleCloudStorage(googleCloudStorageExportRequest);
        assertThat(googleCloudStorageExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse googleCloudStorageExportTaskResponse = googleCloudStorageExportTaskResponseDataResult.getBody().get().getData();
        assertThat(googleCloudStorageExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_GOOGLE_CLOUD_STORAGE);

        // Export Open Stack
        final OpenStackExportRequest openStackExportRequest = new OpenStackExportRequest()
            .setInput(uploadImportTaskResponse.getId()).setAuthUrl("some-auth-url").setUsername("some-username")
            .setPassword("some-password").setRegion("some-region").setContainer("some-container").setFile("some-file");
        final Result<TaskResponseData> openStackExportTaskResponseDataResult = cloudConvertClient.exportUsing().openStack(openStackExportRequest);
        assertThat(openStackExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse openStackExportTaskResponse = openStackExportTaskResponseDataResult.getBody().get().getData();
        assertThat(openStackExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_OPENSTACK);

        // Export SFTP
        final SftpExportRequest sftpExportRequest = new SftpExportRequest().setInput(uploadImportTaskResponse.getId())
            .setHost("some-host").setUsername("some-username").setPassword("some-password").setFile("some-file");
        final Result<TaskResponseData> sftpExportTaskResponseDataResult = cloudConvertClient.exportUsing().sftp(sftpExportRequest);
        assertThat(sftpExportTaskResponseDataResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final TaskResponse sftpExportTaskResponse = sftpExportTaskResponseDataResult.getBody().get().getData();
        assertThat(sftpExportTaskResponse.getOperation()).isEqualTo(Operation.EXPORT_SFTP);
    }

    @After
    public void after() throws Exception {
        jpgTest1InputStream.close();
        cloudConvertClient.close();
    }
}
