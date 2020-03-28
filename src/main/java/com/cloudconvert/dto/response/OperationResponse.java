package com.cloudconvert.dto.response;

import com.cloudconvert.dto.Operation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class OperationResponse extends Response {

    /**
     * Name of the operation, such as convert, optimize or capture-website.
     */
    private Operation operation;

    /**
     * Format of the input file.
     */
    private String inputFormat;

    /**
     * Format of the conversion result.
     */
    private String outputFormat;

    /**
     * Name of the engine.
     */
    private String engine;

    /**
     * Possible options for this conversion type. Available, if the include argument contains options.
     */
    private List<Option> options;

    /**
     * Compatible engine versions for this conversion type. Available, if the include argument contains engine_versions.
     */
    private List<EngineVersion> engineVersions;

    /**
     * This format is deprecated.
     */
    private Boolean deprecated;

    /**
     * This format is experimental.
     */
    private Boolean experimental;

    /**
     * Meta attributes.
     */
    private Map<String, String> meta;


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Option {

        /**
         * Name of the option.
         */
        private String name;

        /**
         * Data type of the option: string, boolean, integer, float, enum or dictionary.
         */
        private String type;

        /**
         * Default value.
         */
        @JsonProperty("default")
        private String defaultValue;

        /**
         * Possible values if the data type is enum.
         */
        private List<String> possibleValues;
    }


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class EngineVersion {

        /**
         * Version of the engine.
         */
        private String version;

        /**
         * This is the default version.
         */
        @JsonProperty("default")
        private Boolean defaultValue;

        /**
         * This version is the latest one.
         */
        private Boolean latest;

        /**
         * This version is deprecated.
         */
        private Boolean deprecated;

        /**
         * This version is experimental.
         */
        private Boolean experimental;
    }
}
