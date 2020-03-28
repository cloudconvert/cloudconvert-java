package com.cloudconvert.resource;

import com.cloudconvert.client.api.key.ApiKeyProvider;
import com.cloudconvert.client.api.url.ApiUrlProvider;
import com.cloudconvert.client.mapper.ObjectMapperProvider;
import com.cloudconvert.dto.request.AzureBlobExportRequest;
import com.cloudconvert.dto.request.GoogleCloudStorageExportRequest;
import com.cloudconvert.dto.request.OpenStackExportRequest;
import com.cloudconvert.dto.request.S3ExportRequest;
import com.cloudconvert.dto.request.SftpExportRequest;
import com.cloudconvert.dto.request.UrlExportRequest;
import com.cloudconvert.dto.response.TaskResponseData;
import com.cloudconvert.dto.result.AbstractResult;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractExportFilesResource<TRDAR extends AbstractResult<TaskResponseData>> extends AbstractResource {

    public static final String PATH_SEGMENT_EXPORT = "export";
    public static final String PATH_SEGMENT_URL = "url";
    public static final String PATH_SEGMENT_S3 = "s3";
    public static final List<String> PATH_SEGMENTS_AZURE_BLOB = ImmutableList.of("azure", "blob");
    public static final String PATH_SEGMENT_GOOGLE_CLOUD_STORAGE = "google-cloud-storage";
    public static final String PATH_SEGMENT_OPENSTACK = "openstack";
    public static final String PATH_SEGMENT_SFTP = "sftp";

    public AbstractExportFilesResource(
        final ApiUrlProvider apiUrlProvider, final ApiKeyProvider apiKeyProvider, final ObjectMapperProvider objectMapperProvider
    ) {
        super(apiUrlProvider, apiKeyProvider, objectMapperProvider);
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
    public abstract TRDAR url(
        @NotNull final UrlExportRequest urlExportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to export files to a S3 bucket.
     *
     * @param s3ExportRequest {@link S3ExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR s3(
        @NotNull final S3ExportRequest s3ExportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to export files to a Azure blob container.
     *
     * @param azureBlobExportRequest {@link AzureBlobExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR azureBlob(
        @NotNull final AzureBlobExportRequest azureBlobExportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to export files to a Google Cloud Storage bucket.
     *
     * @param googleCloudStorageExportRequest {@link GoogleCloudStorageExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR googleCloudStorage(
        @NotNull final GoogleCloudStorageExportRequest googleCloudStorageExportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to export files to OpenStack Object Storage (Swift).
     *
     * @param openStackExportRequest {@link OpenStackExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR openStack(
        @NotNull final OpenStackExportRequest openStackExportRequest
    ) throws IOException, URISyntaxException;

    /**
     * Create a task to export files to a SFTP server.
     *
     * @param sftpExportRequest {@link SftpExportRequest}
     * @return TRD
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract TRDAR sftp(
        @NotNull final SftpExportRequest sftpExportRequest
    ) throws IOException, URISyntaxException;
}
