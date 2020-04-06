package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.*;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractImportFilesResource<TRDAR extends AbstractResult<TaskResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_IMPORT = "import";
    public static final String PATH_SEGMENT_URL = "url";
    public static final String PATH_SEGMENT_UPLOAD = "upload";
    public static final String PATH_SEGMENT_S3 = "s3";
    public static final List<String> PATH_SEGMENTS_AZURE_BLOB = ImmutableList.of("azure", "blob");
    public static final String PATH_SEGMENT_GOOGLE_CLOUD_STORAGE = "google-cloud-storage";
    public static final String PATH_SEGMENT_OPENSTACK = "openstack";
    public static final String PATH_SEGMENT_SFTP = "sftp";

    private final Tika tika;

    public AbstractImportFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);

        this.tika = new Tika();
    }

    /**
     * Create a task to import one file by downloading it from an URL.
     *
     * @param urlImportRequest {@link UrlImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR url(
        @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file immediately
     *
     * @param uploadImportRequest {@link UploadImportRequest}
     * @param inputStream         {@link InputStream} of file which will be uploaded
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR upload(
        @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Just create upload task, do not upload file immediately
     *
     * @param uploadImportRequest {@link UploadImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR upload(
        @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task response data result
     *
     * @param taskResponseDataResult {@link TRDAR}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR upload(
        @NotNull final TRDAR taskResponseDataResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task id and {@link com.cloudconvert.dto.response.TaskResponse.Result.Form}
     *
     * @param taskId                 task
     * @param taskResponseResultForm {@link com.cloudconvert.dto.response.TaskResponse.Result.Form}
     * @param inputStream            {@link InputStream} of file which will be uploaded
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR upload(
        @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from a S3 bucket.
     *
     * @param s3ImportRequest {@link S3ImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR s3(
        @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from a Azure blob container.
     *
     * @param azureBlobImportRequest {@link AzureBlobImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR azureBlob(
        @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from a Google Cloud Storage bucket.
     *
     * @param googleCloudStorageImportRequest {@link GoogleCloudStorageImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR googleCloudStorage(
        @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from OpenStack Object Storage (Swift).
     *
     * @param openStackImportRequest {@link OpenStackImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR openStack(
        @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from a SFTP server.
     *
     * @param sftpImportRequest {@link SftpImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR sftp(
        @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpEntity getMultipartHttpEntity(
        final TaskResponse.Result.Form.Parameters uploadImportResponseResultFormParameters, final InputStream inputStream
    ) throws IOException {
        try {
            final MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStream));

            final HttpEntity httpEntity = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addTextBody("expires", uploadImportResponseResultFormParameters.getExpires())
                .addTextBody("max_file_count", uploadImportResponseResultFormParameters.getMaxFileCount())
                .addTextBody("max_file_size", uploadImportResponseResultFormParameters.getMaxFileSize())
                .addTextBody("redirect", uploadImportResponseResultFormParameters.getRedirect())
                .addTextBody("signature", uploadImportResponseResultFormParameters.getSignature())
                .addPart(FormBodyPartBuilder.create("form", new InputStreamBody(inputStream, "file" + mimeType.getExtension())).build()).build();

            return new BufferedHttpEntity(httpEntity);
        } catch (MimeTypeException e) {
            throw new IOException(e);
        }
    }
}
