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
public class AzureBlobExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to export. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The name of the Azure storage account (This is the string before .blob.core.windows.net ).
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
     * (optional) Blob name for storing the file (the filename in the container, including path).
     * If there are multiple files to export, printf style placeholders are possible (e.g. myfile-%d.pdf produces the output files myfile-1.pdf, myfile-2.pdf and so on).
     */
    private String blob;

    /**
     * (optional) Alternatively to using blob, you can specify a blob prefix for exporting files.
     */
    private String blobPrefix;

    public AzureBlobExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_AZURE_BLOB;
    }
}
