package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AzureBlobImportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageImportRequest;
import com.cloudconvert.dto.request.OpenStackImportRequest;
import com.cloudconvert.dto.request.S3ImportRequest;
import com.cloudconvert.dto.request.SftpImportRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.request.UrlImportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractImportFilesResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

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
    public abstract TRAR url(
            @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getUrlHttpUriRequest(
            @NotNull final UrlImportRequest urlImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_URL));
        final HttpEntity httpEntity = getHttpEntity(urlImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

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
    public abstract TRAR upload(
            @NotNull final UploadImportRequest uploadImportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file immediately
     *
     * @param uploadImportRequest {@link UploadImportRequest}
     * @param file                {@link File} file which will be uploaded
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final UploadImportRequest uploadImportRequest, @NotNull final File file
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task response data result
     *
     * @param taskResponseResult {@link TRAR}
     * @param file               {@link File} file which will be uploaded
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final TRAR taskResponseResult, @NotNull final File file
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task id and {@link com.cloudconvert.dto.response.TaskResponse.Result.Form}
     *
     * @param taskId                 task
     * @param taskResponseResultForm {@link com.cloudconvert.dto.response.TaskResponse.Result.Form}
     * @param file                   {@link File} file which will be uploaded
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final File file
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
    public abstract TRAR upload(
            @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file immediately
     *
     * @param uploadImportRequest {@link UploadImportRequest}
     * @param inputStream         {@link InputStream} of file which will be uploaded
     * @param filename            Set the filename, including file extension
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final UploadImportRequest uploadImportRequest, @NotNull final InputStream inputStream, @NotNull String filename
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task response data result
     *
     * @param taskResponseResult {@link TRAR}
     * @param inputStream        {@link InputStream} of file which will be uploaded
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final TRAR taskResponseResult, @NotNull final InputStream inputStream
    ) throws IOException, URISyntaxException;

    /**
     * Create a task which uploads one input file.
     * It allows your users to directly upload input files to CloudConvert, without temporary storing them on your server.
     * <p>
     * Upload file using existing task response data result
     *
     * @param taskResponseResult {@link TRAR}
     * @param inputStream        {@link InputStream} of file which will be uploaded
     * @param filename           Set the filename, including file extension
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final TRAR taskResponseResult, @NotNull final InputStream inputStream, @NotNull final String filename
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
    public abstract TRAR upload(
            @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream
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
     * @param filename               Set the filename, including file extension
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR upload(
            @NotNull final String taskId, @NotNull final TaskResponse.Result.Form taskResponseResultForm, @NotNull final InputStream inputStream, @NotNull final String filename
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to import files by downloading it from a S3 bucket.
     *
     * @param s3ImportRequest {@link S3ImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR s3(
            @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getS3HttpUriRequest(
            @NotNull final S3ImportRequest s3ImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_S3));
        final HttpEntity httpEntity = getHttpEntity(s3ImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to import files by downloading it from a Azure blob container.
     *
     * @param azureBlobImportRequest {@link AzureBlobImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR azureBlob(
            @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getAzureBlobHttpUriRequest(
            @NotNull final AzureBlobImportRequest azureBlobImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.<String>builder().add(PATH_SEGMENT_IMPORT).addAll(PATH_SEGMENTS_AZURE_BLOB).build());
        final HttpEntity httpEntity = getHttpEntity(azureBlobImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to import files by downloading it from a Google Cloud Storage bucket.
     *
     * @param googleCloudStorageImportRequest {@link GoogleCloudStorageImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR googleCloudStorage(
            @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getGoogleCloudHttpUriRequest(
            @NotNull final GoogleCloudStorageImportRequest googleCloudStorageImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_GOOGLE_CLOUD_STORAGE));
        final HttpEntity httpEntity = getHttpEntity(googleCloudStorageImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to import files by downloading it from OpenStack Object Storage (Swift).
     *
     * @param openStackImportRequest {@link OpenStackImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR openStack(
            @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getOpenStackHttpUriRequest(
            @NotNull final OpenStackImportRequest openStackImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_OPENSTACK));
        final HttpEntity httpEntity = getHttpEntity(openStackImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to import files by downloading it from a SFTP server.
     *
     * @param sftpImportRequest {@link SftpImportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR sftp(
            @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getSftpHttpUriRequest(
            @NotNull final SftpImportRequest sftpImportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_IMPORT, PATH_SEGMENT_SFTP));
        final HttpEntity httpEntity = getHttpEntity(sftpImportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    protected HttpEntity getMultipartHttpEntity(
            final TaskResponse.Result.Form uploadImportResponseResultForm, final File file
    ) throws IOException {
        // We still need to convert File to InputStream here, because if we don't, then async client will fail with ContentTooLongException if we do not
        return getMultipartHttpEntity(uploadImportResponseResultForm, file.getName(), new FileInputStream(file));
    }

    protected HttpEntity getMultipartHttpEntity(
            final TaskResponse.Result.Form uploadImportResponseResultForm, final InputStream inputStream
    ) throws IOException {
        try {
            final MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(tika.detect(inputStream));
            return getMultipartHttpEntity(uploadImportResponseResultForm, "file" + mimeType.getExtension(), inputStream);
        } catch (MimeTypeException e) {
            throw new IOException(e);
        }
    }

    protected HttpEntity getMultipartHttpEntity(
            final TaskResponse.Result.Form uploadImportResponseResultForm,
            @NotNull final String filename, final InputStream inputStream
    ) throws IOException {
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setContentType(ContentType.MULTIPART_FORM_DATA);
        uploadImportResponseResultForm.getParameters().forEach(multipartEntityBuilder::addTextBody);
        return new BufferedHttpEntity(multipartEntityBuilder.addPart(
                FormBodyPartBuilder.create("file", new InputStreamBody(new BufferedInputStream(inputStream), filename)).build()).build());
    }
}
