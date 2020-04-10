package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import com.fasterxml.jackson.annotation.JsonValue;
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
public class CaptureWebsitesTaskRequest extends TaskRequest {

    /**
     * (required) The URL of the website.
     */
    private String url;

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
    /**
     * (optional) load: consider navigation to be finished when the load event is fired.
     * domcontentloaded: consider navigation to be finished when the DOMContentLoaded event is fired.
     * networkidle0: consider navigation to be finished when there are no more than 0 network connections for at least 500 ms.
     * networkidle2: consider navigation to be finished when there are no more than 2 network connections for at least 500 ms.
     * Defaults to load. Possible values: load, domcontentloaded, networkidle0, networkidle2.
     */
    private WaitUntil waitUntil;

    /**
     * (optional) CSS selector for element to wait for (e.g. "body" or "#element").
     */
    private String waitForElement;

    /**
     * (optional) Additional time in ms to wait after the page load.
     */
    private Integer waitTime;

    /**
     * (optional) Object of additional headers to send with the request. Can be used to access URLs that require authorization.
     */
    private Map<String, String> headers;

    @Override
    public Operation getOperation() {
        return Operation.CAPTURE_WEBSITE;
    }

    public enum WaitUntil {

        LOAD("load"),
        DOMCONTENTLOADED("domcontentloaded"),
        NETWORKIDLE_0("networkidle0"),
        NETWORKIDLE_2("networkidle2");

        @Getter
        @JsonValue
        private final String label;

        WaitUntil(final String label) {
            this.label = label;
        }
    }
}
