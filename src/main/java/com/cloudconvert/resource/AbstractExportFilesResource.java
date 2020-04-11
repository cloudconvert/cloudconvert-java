package com.cloudconvert.resource;

import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.client.setttings.SettingsProvider;
import com.cloudconvert.dto.request.AzureBlobExportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageExportRequest;
import com.cloudconvert.dto.request.OpenStackExportRequest;
import com.cloudconvert.dto.request.S3ExportRequest;
import com.cloudconvert.dto.request.SftpExportRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.AbstractResult;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractExportFilesResource<TRAR extends AbstractResult<TaskResponse>> extends AbstractResource {

    public static final String PATH_SEGMENT_EXPORT = "export";
    public static final String PATH_SEGMENT_URL = "url";
    public static final String PATH_SEGMENT_S3 = "s3";
    public static final List<String> PATH_SEGMENTS_AZURE_BLOB = ImmutableList.of("azure", "blob");
    public static final String PATH_SEGMENT_GOOGLE_CLOUD_STORAGE = "google-cloud-storage";
    public static final String PATH_SEGMENT_OPENSTACK = "openstack";
    public static final String PATH_SEGMENT_SFTP = "sftp";

    public AbstractExportFilesResource(
        final SettingsProvider settingsProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(settingsProvider, objectMapperProvider);
    }

    /**
     * This task creates temporary URLs which can be used to download the files.
     * <p>
     * Please note that all tasks get deleted after 24 hours automatically. Meaning, the created URLs are available for 24 hours only.
     *
     * @param urlExportRequest {@link UrlExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR url(
        @NotNull final UrlExportRequest urlExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getUrlHttpUriRequest(
        @NotNull final UrlExportRequest urlExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_EXPORT, PATH_SEGMENT_URL));
        final HttpEntity httpEntity = getHttpEntity(urlExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to export files to a S3 bucket.
     *
     * @param s3ExportRequest {@link S3ExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR s3(
        @NotNull final S3ExportRequest s3ExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getS3HttpUriRequest(
        @NotNull final S3ExportRequest s3ExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_EXPORT, PATH_SEGMENT_S3));
        final HttpEntity httpEntity = getHttpEntity(s3ExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to export files to a Azure blob container.
     *
     * @param azureBlobExportRequest {@link AzureBlobExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR azureBlob(
        @NotNull final AzureBlobExportRequest azureBlobExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getAzureBlobHttpUriRequest(
        @NotNull final AzureBlobExportRequest azureBlobExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.<String>builder().add(PATH_SEGMENT_EXPORT).addAll(PATH_SEGMENTS_AZURE_BLOB).build());
        final HttpEntity httpEntity = getHttpEntity(azureBlobExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to export files to a Google Cloud Storage bucket.
     *
     * @param googleCloudStorageExportRequest {@link GoogleCloudStorageExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR googleCloudStorage(
        @NotNull final GoogleCloudStorageExportRequest googleCloudStorageExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getGoogleCloudStorageHttpUriRequest(
        @NotNull final GoogleCloudStorageExportRequest googleCloudStorageExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_EXPORT, PATH_SEGMENT_GOOGLE_CLOUD_STORAGE));
        final HttpEntity httpEntity = getHttpEntity(googleCloudStorageExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to export files to OpenStack Object Storage (Swift).
     *
     * @param openStackExportRequest {@link OpenStackExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR openStack(
        @NotNull final OpenStackExportRequest openStackExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getOpenStackStorageHttpUriRequest(
        @NotNull final OpenStackExportRequest openStackExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_EXPORT, PATH_SEGMENT_OPENSTACK));
        final HttpEntity httpEntity = getHttpEntity(openStackExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }

    /**
     * Create a task to export files to a SFTP server.
     *
     * @param sftpExportRequest {@link SftpExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRAR sftp(
        @NotNull final SftpExportRequest sftpExportRequest
    ) throws IOException, URISyntaxException;

    protected HttpUriRequest getSftpStorageHttpUriRequest(
        @NotNull final SftpExportRequest sftpExportRequest
    ) throws IOException, URISyntaxException {
        final URI uri = getUri(ImmutableList.of(PATH_SEGMENT_EXPORT, PATH_SEGMENT_SFTP));
        final HttpEntity httpEntity = getHttpEntity(sftpExportRequest);

        return getHttpUriRequest(HttpPost.class, uri, httpEntity);
    }
}
