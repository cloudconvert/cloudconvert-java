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
public class UploadImportRequest extends TaskRequest {

    /**
     * (required) Optionally redirect user to this URL after upload.
     */
    private String redirect;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_UPLOAD;
    }
}
