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
public class WriteMetadataTaskRequest extends TaskRequest {

    /**
     * (required) The ID of the input task for the conversion, normally the import task. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (optional) The current format of the file, e.g. pdf. If not set, the extension of the input file is used as input format.
     */
    private String inputFormat;

    /**
     * (optional) Use a specific engine for the conversion.
     */
    private String engine;

    /**
     * (required) Keys and values of metadata to write. Possible keys include Title, Author, Creator, Producer.
     */
    private Map<String, String> metadata;

    /**
     * (optional) Use a specific engine version for the conversion.
     */
    private String engineVersion;


    public WriteMetadataTaskRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.METADATA_WRITE;
    }
}
