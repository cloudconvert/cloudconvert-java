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
public class S3ImportRequest extends TaskRequest {

    /**
     * (required) The Amazon S3 bucket where to download the file.
     */
    private String bucket;

    /**
     * (required) Specify the Amazon S3 endpoint, e.g. us-west-2 or eu-west-1.
     */
    private String region;

    /**
     * (optional) Use a custom S3 API endpoint. The default endpoint is built from the configured region.
     * Makes it possible to use other S3 compatible storage services (e.g. DigitalOcean).
     */
    private String endpoint;

    /**
     * (optional) S3 key of the input file (the filename in the bucket, including path).
     */
    private String key;

    /**
     * (optional) Alternatively to using key, you can specify a key prefix for importing multiple files at once.
     */
    private String keyPrefix;

    /**
     * (required) The Amazon S3 access key id. It needs to have the s3:GetObject permission.
     */
    private String accessKeyId;

    /**
     * (required) The Amazon S3 secret access key.
     */
    private String secretAccessKey;

    /**
     * (optional) Auth using temporary credentials (AWS Security Token Service).
     */
    private String sessionToken;

    /**
     * (optional) The filename of the input file, including extension. If none provided we will use the key parameter as the filename for the file.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_S3;
    }
}
