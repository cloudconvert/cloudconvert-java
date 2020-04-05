package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class S3ExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to export. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The Amazon S3 bucket where to store the file(s).
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
     * (optional) S3 key for storing the file (the filename in the bucket, including path).
     * If there are multiple files to export, printf style placeholders are possible (e.g. myfile-%d.pdf produces the output files myfile-1.pdf, myfile-2.pdf and so on).
     */
    private String key;

    /**
     * (optional) Alternatively to using key, you can specify a key prefix for exporting files.
     */
    private String keyPrefix;

    /**
     * (required) The Amazon S3 access key id. It needs to have the s3:PutObject permission.
     * When using a different ACL from private, it needs to have the s3:PutObjectAcl permission.
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
     * (optional) S3 ACL for storing the files.
     * Possible values include: private, public-read, public-read-write, authenticated-read, bucket-owner-read, bucket-owner-full-control. Defaults to private.
     */
    private String acl;

    /**
     * (optional) S3 CacheControl header to specify the lifetime of the file, for example: max-age=172800.
     */
    private String cacheControl;

    /**
     * (optional) Object of additional S3 meta data.
     */
    private Map<String, String> metadata;

    /**
     * (optional) Enable the Server-side encryption algorithm used when storing this object in S3. Possible values include AES256 and aws:kms.
     */
    private String serverSideEncryption;

    public S3ExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_S3;
    }
}
