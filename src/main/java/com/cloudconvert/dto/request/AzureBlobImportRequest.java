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
public class AzureBlobImportRequest extends TaskRequest {

    /**
     * (required) The name of the Azure storage account (This is the string before .blob.core.windows.net).
     */
    private String storageAccount;

    /**
     * (optional) The Azure secret key. Only required alternatively, if you are not providing a SAS token.
     */
    private String storageAccessKey;

    /**
     * (optional) The Azure SAS token.
     */
    private String sasToken;

    /**
     * (required) Azure container name.
     */
    private String container;

    /**
     * (optional) Azure blob name of the input file (the filename in the bucket, including path).
     */
    private String blob;

    /**
     * (required) Alternatively to using blob, you can specify a blob prefix for importing multiple files at once.
     */
    private String blobPrefix;

    /**
     * (required) The filename of the input file, including extension. If none provided we will use the blob parameter as the filename for the file.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_AZURE_BLOB;
    }
}
