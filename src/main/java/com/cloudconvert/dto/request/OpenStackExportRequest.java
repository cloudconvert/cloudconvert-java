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
public class OpenStackExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to export. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The URL of the OpenStack Identity endpoint (without version).
     */
    private String authUrl;

    /**
     * (required) The OpenStack username.
     */
    private String username;

    /**
     * (required) The OpenStack password.
     */
    private String password;

    /**
     * (required) Specify the OpenStack region.
     */
    private String region;

    /**
     * (required) The name of the OpenStack Storage container.
     */
    private String container;

    /**
     * (optional) File name of the file to create (the filename in container bucket, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a file prefix for exporting files.
     */
    private String filePrefix;

    public OpenStackExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_OPENSTACK;
    }
}
