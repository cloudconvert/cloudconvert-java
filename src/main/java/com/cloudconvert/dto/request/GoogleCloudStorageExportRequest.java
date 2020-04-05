package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class GoogleCloudStorageExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to export. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The Google Cloud Project ID (api-project-...).
     */
    private String projectId;

    /**
     * (required) The Google Cloud Storage Bucket name.
     */
    private String bucket;

    /**
     * (required) The client email of the service account to use (...@api-project-....iam.gserviceaccount.com).
     */
    private String clientEmail;

    /**
     * (required) The private key of the service account.
     */
    private String privateKey;

    /**
     * (optional) Filename of the file to create (the filename in the bucket, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a file prefix for exporting files.
     */
    private String filePrefix;

    public GoogleCloudStorageExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_GOOGLE_CLOUD_STORAGE;
    }
}
