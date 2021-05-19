package com.cloudconvert.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum Operation {

    CONVERT("convert"),
    OPTIMIZE("optimize"),
    CAPTURE_WEBSITE("capture-website"),
    MERGE("merge"),
    ARCHIVE("archive"),
    ARCHIVE_EXTRACT("archive/extract"),
    COMMAND("command"),
    THUMBNAIL("thumbnail"),
    METADATA("metadata"),
    METADATA_WRITE("metadata/write"),

    IMPORT_URL("import/url"),
    IMPORT_UPLOAD("import/upload"),
    IMPORT_S3("import/s3"),
    IMPORT_AZURE_BLOB("import/azure-blob"),
    IMPORT_GOOGLE_CLOUD_STORAGE("import/google-cloud-storage"),
    IMPORT_OPENSTACK("import/openstack"),
    IMPORT_SFTP("import/sftp"),
    IMPORT_BASE64("import/base64"),
    IMPORT_RAW("import/raw"),

    EXPORT_URL("export/url"),
    EXPORT_S3("export/s3"),
    EXPORT_AZURE_BLOB("export/azure-blob"),
    EXPORT_GOOGLE_CLOUD_STORAGE("export/google-cloud-storage"),
    EXPORT_OPENSTACK("export/openstack"),
    EXPORT_SFTP("export/sftp");

    @Getter
    @JsonValue
    private final String label;

    Operation(final String label) {
        this.label = label;
    }
}