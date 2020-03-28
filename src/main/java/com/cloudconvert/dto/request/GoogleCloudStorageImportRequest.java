package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class GoogleCloudStorageImportRequest extends TaskRequest {

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
     * (optional) Filename of the input file (the filename in the bucket, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a file prefix for importing multiple files at once.
     */
    private String filePrefix;

    /**
     * (optional) The filename of the input file, including extension. If none provided we will use the file parameter as the filename for the file.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_GOOGLE_CLOUD_STORAGE;
    }
}
