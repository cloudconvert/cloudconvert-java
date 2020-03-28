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
public class CreateArchivesTaskRequest extends TaskRequest {

    /**
     * (required) The ID of the input task for the conversion, normally the import task. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The target format to convert to.
     */
    private String outputFormat;

    /**
     * (optional) Use a specific engine for the conversion.
     */
    private String engine;

    /**
     * (optional) Use a specific engine version for the conversion.
     */
    private String engineVersion;

    /**
     * (optional) Choose a filename (including extension) for the output file.
     */
    private String filename;

    public CreateArchivesTaskRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.ARCHIVE;
    }
}
