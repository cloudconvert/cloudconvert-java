package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class UrlImportRequest extends TaskRequest {

    /**
     * (required) The URL of the website.
     */
    private String url;

    /**
     * (optional) The filename of the input file, including extension. If none provided we will try to detect the filename from the URL.
     */
    private String filename;

    /**
     * (optional) Object of additional headers to send with the download request. Can be used to access URLs that require authorization.
     */
    private Map<String, String> headers;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_URL;
    }
}
