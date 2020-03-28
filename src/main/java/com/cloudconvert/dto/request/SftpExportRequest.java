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
public class SftpExportRequest extends TaskRequest {

    /**
     * (required) The ID of the task to export. Multiple task IDs can be provided as an array.
     */
    private List<String> input;

    /**
     * (required) The SFTP server hostname.
     */
    private String host;

    /**
     * (optional) The SFTP port. Defaults to 22.
     */
    private String port;

    /**
     * (required) The SFTP username.
     */
    private String username;

    /**
     * (optional) The SFTP password.
     */
    private String password;

    /**
     * (optional) Alternatively to using password, you can provide a private key.
     */
    private String privateKey;

    /**
     * (optional) File name of the file to create (the filename on the server, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a path for exporting files.
     */
    private String path;

    public SftpExportRequest setInput(final String... input) {
        this.input = Arrays.stream(input).collect(Collectors.toList());
        return this;
    }

    @Override
    public Operation getOperation() {
        return Operation.EXPORT_SFTP;
    }
}
