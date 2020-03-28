package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import com.fasterxml.jackson.annotation.JsonValue;
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
public class OptimizeFilesTaskRequest extends TaskRequest {

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
     * (optional) Use a specific engine version for the conversion.
     */
    private String engineVersion;

    /**
     * (optional) Choose a filename (including extension) for the output file.
     */
    private String filename;

    /**
     * (optional) Optimization profile for specific target needs. Defaults to web. Possible values: web, print, archive, mrc, max.
     */
    private Profile profile;

    public OptimizeFilesTaskRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.OPTIMIZE;
    }

    public enum Profile {

        /**
         * Optimization for the web (Default profile)
         * - Remove redundant and unnecessary data for the web
         * - Down-sample, clip, and intelligently compress images
         * - Merge and subset fonts
         * - Convert colors to RGB
         */
        WEB("web"),

        /**
         * Optimization for print
         * - Remove redundant and unnecessary data for printing
         * - Down-sample, clip, and intelligently compress images
         * - Merge and subset fonts
         * - Convert colors to CMYK
         */
        PRINT("print"),

        /**
         * Optimization for archiving purposes
         * - Remove redundant and unnecessary data for archiving
         * - Intelligently compress images
         * - Merge and subset fonts
         */
        ARCHIVE("archive"),

        /**
         * Optimization for scanned images
         * - Profile optimized for PDFs that mainly consist of raster images
         * - Compress the images without significantly reducing the visual quality
         */
        MRC("mrc"),

        /**
         * Optimization for maximal size reduction
         * - Use the maximal possible compression
         * - Might reduce the visual quality
         */
        MAX("max");


        @Getter
        @JsonValue
        private final String label;

        Profile(final String label) {
            this.label = label;
        }
    }
}
