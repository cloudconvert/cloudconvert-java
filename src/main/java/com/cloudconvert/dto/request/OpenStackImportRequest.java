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
public class OpenStackImportRequest extends TaskRequest {

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
     * (optional) File name of the input file (the filename in the container, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a file prefix for importing multiple files at once.
     */
    private String filePrefix;

    /**
     * (optional) The filename of the input file, including extension. If none provided we will use the file parameter as the filename for the file.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_OPENSTACK;
    }
}
