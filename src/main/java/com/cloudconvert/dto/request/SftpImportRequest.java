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
public class SftpImportRequest extends TaskRequest {

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
    private String private_key;

    /**
     * (optional) File name of the input file (the filename on the server, including path).
     */
    private String file;

    /**
     * (optional) Alternatively to using file, you can specify a path for importing multiple files at once.
     */
    private String path;

    /**
     * (optional) The filename of the input file, including extension. If none provided we will use the file parameter as the filename for the file.
     */
    private String filename;

    @Override
    public Operation getOperation() {
        return Operation.IMPORT_SFTP;
    }
}
