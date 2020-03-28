## cloudconvert-java

This is the official Java SDK v2 for the [CloudConvert](https://cloudconvert.com/api/v2) _API v2_.

[![Build Status](https://travis-ci.org/cloudconvert/cloudconvert-java.svg?branch=master)](https://travis-ci.org/cloudconvert/cloudconvert-java)

## Installation
```
$ mvn clean install -U
```

## Creating API Client
###### Configuration
By default, API Key and API URL are being read from `application.properties` file
```properties
CLOUDCONVERT_API_KEY=<api-key>
CLOUDCONVERT_API_URL=<api-url>
```
It is also possible to provide API Key and API URL using environment variables, custom properties file, system properties and string variables.
For all options, `CLOUDCONVERT_API_KEY` and `CLOUDCONVERT_API_URL` variable names should be used

###### Default (synchronous) client
```java
// Using API Key and API URL from `application.properties` file
new CloudConvertClient();

// Using API Key and API URL from environment variables
new CloudConvertClient(new EnvironmentVariableApiUrlProvider(), new EnvironmentVariableApiKeyProvider());

// Using API Key and API URL from custom properties file
new CloudConvertClient(new PropertiesFileApiUrlProvider("custom.properties"), new PropertiesFileApiKeyProvider("custom.properties"));

// Using API Key and API URL from string variables
new CloudConvertClient(new StringApiUrlProvider("api-url"), new StringApiKeyProvider("api-key"));

// Using API Key and API URL from system properties
new CloudConvertClient(new SystemPropertyApiUrlProvider(), new SystemPropertyApiKeyProvider());
```

###### Asynchronous client
```java
// Using API Key and API URL from `application.properties` file
new AsyncCloudConvertClient();

// Using API Key and API URL from environment variables
new AsyncCloudConvertClient(new EnvironmentVariableApiUrlProvider(), new EnvironmentVariableApiKeyProvider());

// Using API Key and API URL from custom properties file
new AsyncCloudConvertClient(new PropertiesFileApiUrlProvider("custom.properties"), new PropertiesFileApiKeyProvider("custom.properties"));

// Using API Key and API URL from string variables
new AsyncCloudConvertClient(new StringApiUrlProvider("api-url"), new StringApiKeyProvider("api-key"));

// Using API Key and API URL from system properties
new AsyncCloudConvertClient(new SystemPropertyApiUrlProvider(), new SystemPropertyApiKeyProvider());
```

## Creating Jobs
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// Create a job
final JobResponse jobResponse = cloudConvertClient.jobs().create(
    ImmutableMap.of(
        "import-my-file", new UrlImportRequest().setUrl("import-url"),
        "convert-my-file", new ConvertFilesTaskRequest().setInput("import-my-file"),
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
    )
).getBody().get().getData();
```

## Downloading Files
CloudConvert can generate public URLs for using `export/url` tasks. You can use these URLs to download output files.
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// Task id of export/url task
final String exportUrlTaskId = "84e872fc-d823-4363-baab-eade2e05ee54"; 

// Wait for export/url task to be finished
final TaskResponse waitUrlExportTaskResponse = cloudConvertClient.tasks().wait("").getBody().get().getData();

// Get url of export/url task
final String exportUrl = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url");

// Get file as input stream using url of export/url task
final InputStream inputStream = cloudConvertClient.files().download(exportUrl).getBody().get();
```

## Uploading Files
Uploads to CloudConvert are done via `import/upload` tasks (see the [docs](https://cloudconvert.com/api/v2/import#import-upload-tasks)).
This SDK offers a convenient upload method:
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// File as input stream
final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file.jpg");

// Upload file using import/upload task
final TaskResponse uploadImportTaskResponse = cloudConvertClient.importUsing().upload(new UploadImportRequest(), inputStream).getBody().get().getData();

// Wait for import/upload task to be finished
final TaskResponse waitUploadImportTaskResponse = cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()).getBody().get().getData();
```

## Signing Webhook 
The node SDK allows to verify webhook requests received from CloudConvert.
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// The JSON payload from the raw request body.
final String payload = "payload";

// The value of the "CloudConvert-Signature" header.
final String signature = "signature";

// You can find it in your webhook settings.
final String secret = "secret";

// Returns true if signature is valid, and false if signature is invalid
final boolean isValid = cloudConvertClient.webhooks().verify(payload, signature, secret);
```

## Unit Tests
```
$ mvn clean install -U -Punit-tests
```

## Integration Tests
```
$ mvn clean install -U -Pintegration-tests
```
       
## Resources
* [API v2 Documentation](https://cloudconvert.com/api/v2)
* [CloudConvert Blog](https://cloudconvert.com/blog)
