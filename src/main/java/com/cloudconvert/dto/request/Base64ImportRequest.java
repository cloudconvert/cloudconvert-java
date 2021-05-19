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
public class Base64ImportRequest extends TaskRequest{

    /***
     * (required) The base64 encoded file content.
     */
    private String file;

    /***
     * (required) The filename of the input file, including extension.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_BASE64;
    }
}
