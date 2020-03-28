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
public class UrlExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to create temporary URLs for. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (optional) This option makes the export URLs return the Content-Disposition inline header, which tells browser to display the file instead of downloading it.
     */
    private Boolean inline;

    /**
     * (optional) By default, multiple files will create multiple export URLs. When enabling this option, one export URL with a ZIP file will be created.
     */
    private Boolean archiveMultipleFiles;

    public UrlExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_URL;
    }
}
