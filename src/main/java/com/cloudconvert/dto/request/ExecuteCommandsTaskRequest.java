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
public class ExecuteCommandsTaskRequest extends TaskRequest {

    /**
     * (required) The ID of the input task for the conversion, normally the import task. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (optional) Use a specific engine for the conversion.
     */
    private Engine engine;

    /**
     * (optional) Use a specific engine version for the conversion.
     */
    private String engineVersion;

    /**
     * (optional) Capture the console output of the command and return it in the results object.
     */
    private Boolean captureOutput;

    /**
     * (optional) Command. Currently, ffmpeg, imagemagick and graphicsmagick commands re supported.
     */
    private Command command;

    /**
     * (optional) Command arguments.
     */
    private String arguments;

    public ExecuteCommandsTaskRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.COMMAND;
    }

    public enum Engine {

        FFMPEG("ffmpeg"),
        GRAPHICSMAGICK("graphicsmagick"),
        IMAGEMAGICK("imagemagick");

        @Getter
        @JsonValue
        private final String label;

        Engine(final String label) {
            this.label = label;
        }
    }


    public enum Command {

        FFMPEG("ffmpeg"),
        FFPROBE("ffprobe"),
        GM("gm"),
        CONVERT("convert"),
        IDENTIFY("identify");

        @Getter
        @JsonValue
        private final String label;

        Command(final String label) {
            this.label = label;
        }
    }
}
