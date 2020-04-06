## cloudconvert-java
This is the official Java SDK v2 for the [CloudConvert](https://cloudconvert.com/api/v2) _API v2_.
[![Build Status](https://travis-ci.org/cloudconvert/cloudconvert-java.svg?branch=master)](https://travis-ci.org/cloudconvert/cloudconvert-java)

## Installation
```
$ mvn clean install -U
```

## Creating API Client

###### Configuration
By default, API Key and Webhook Signing Secret are being read from `application.properties` file
```properties
CLOUDCONVERT_API_KEY=<api-key>
CLOUDCONVERT_WEBHOOK_SIGNING_SECRET=<api-url>
```
It is also possible to provide API Key and Webhook Signing Secret using environment variables, custom properties file, system properties and string variables.
For all options, `CLOUDCONVERT_API_KEY` and `CLOUDCONVERT_WEBHOOK_SIGNING_SECRET` variable names should be used.
It is also possible to use CloudConvert client in the sandbox environment, by passing `useSandbox=true` variable during object construction.

###### Default (synchronous) client
```java
// Using API Key and API URL from `application.properties` file
new CloudConvertClient(); // Live
new CloudConvertClient(true); // Sandbox

// Using API Key and API URL from environment variables
new CloudConvertClient(new EnvironmentVariableSettingsProvider()); // Live
new CloudConvertClient(new EnvironmentVariableSettingsProvider(true)); // Sandbox

// Using API Key and API URL from custom properties file
new CloudConvertClient(new PropertyFileSettingsProvider("custom.properties")); // Live
new CloudConvertClient(new PropertyFileSettingsProvider("custom.properties", true)); // Sandbox

// Using API Key and API URL from string variables
new CloudConvertClient(new StringSettingsProvider("api-url", "webhook-signing-secret")); // Live
new CloudConvertClient(new StringSettingsProvider("api-url", "webhook-signing-secret", true)); // Sandbox

// Using API Key and API URL from system properties
new CloudConvertClient(new SystemPropertySettingsProvider()); // Live
new CloudConvertClient(new SystemPropertySettingsProvider(true)); // Sandbox
```

###### Asynchronous client
```java
// Using API Key and API URL from `application.properties` file
new AsyncCloudConvertClient(); // Live
new AsyncCloudConvertClient(true); // Sandbox

// Using API Key and API URL from environment variables
new AsyncCloudConvertClient(new EnvironmentVariableSettingsProvider()); // Live
new AsyncCloudConvertClient(new EnvironmentVariableSettingsProvider(true)); // Sandbox

// Using API Key and API URL from custom properties file
new AsyncCloudConvertClient(new PropertyFileSettingsProvider("custom.properties")); // Live
new AsyncCloudConvertClient(new PropertyFileSettingsProvider("custom.properties", true)); // Sandbox

// Using API Key and API URL from string variables
new AsyncCloudConvertClient(new StringSettingsProvider("api-url", "webhook-signing-secret")); // Live
new AsyncCloudConvertClient(new StringSettingsProvider("api-url", "webhook-signing-secret", true)); // Sandbox

// Using API Key and API URL from system properties
new AsyncCloudConvertClient(new SystemPropertySettingsProvider()); // Live
new AsyncCloudConvertClient(new SystemPropertySettingsProvider(true)); // Sandbox
```

## Creating Jobs

###### Default (synchronous) client
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// Create a job
final JobResponse createJobResponse = cloudConvertClient.jobs().create(
    ImmutableMap.of(
        "import-my-file", new UrlImportRequest().setUrl("import-url"),
        "convert-my-file", new ConvertFilesTaskRequest().setInput("import-my-file"),
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
    )
).getBody().get().getData();

// Get a job id
final String jobId = createJobResponse.getId();

// Wait for a job completion
final JobResponse waitJobResponse = cloudConvertClient.jobs().wait(jobId).getBody().get().getData();
```

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new AsyncCloudConvertClient();

// Create a job
final JobResponse createJobResponse = asyncCloudConvertClient.jobs().create(
    ImmutableMap.of(
        "import-my-file", new UrlImportRequest().setUrl("import-url"),
        "convert-my-file", new ConvertFilesTaskRequest().setInput("import-my-file"),
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
    )
).get().getBody().get().getData();

// Get a job id
final String jobId = createJobResponse.getId();

// Wait for a job completion
final JobResponse waitJobResponse = asyncCloudConvertClient.jobs().wait(jobId).get().getBody().get().getData();
```

## Downloading Files
CloudConvert can generate public URLs using `export/url` tasks. You can use these URLs to download output files.

###### Default (synchronous) client
```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// Create an export/url task
final TaskResponse exportUrlTaskResponse = cloudConvertClient.exportUsing().url(new UrlExportRequest()).getBody().get().getData();

// Get an export/url task id
final String exportUrlTaskId = exportUrlTaskResponse.getId();

// Wait for an export/url task to be finished
final TaskResponse waitUrlExportTaskResponse = cloudConvertClient.tasks().wait(exportUrlTaskId).getBody().get().getData();

// Get a url of export/url task
final String exportUrl = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url");

// Get file as input stream using url of export/url task
final InputStream inputStream = cloudConvertClient.files().download(exportUrl).getBody().get();
```

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new AsyncCloudConvertClient();

// Create an export/url task
final TaskResponse exportUrlTaskResponse = asyncCloudConvertClient.exportUsing().url(new UrlExportRequest()).get()getBody().get().getData();

// Get an export/url task id
final String exportUrlTaskId = exportUrlTaskResponse.getId();

// Wait for an export/url task to be finished
final TaskResponse waitUrlExportTaskResponse = asyncCloudConvertClient.tasks().wait(exportUrlTaskId).get().getBody().get().getData();

// Get a url of export/url task
final String exportUrl = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url");

// Get file as input stream using url of export/url task
final InputStream inputStream = asyncCloudConvertClient.files().download(exportUrl).get().getBody().get();
```

## Uploading Files
Uploads to CloudConvert are done via `import/upload` tasks (see the [docs](https://cloudconvert.com/api/v2/import#import-upload-tasks)).
This SDK offers a convenient upload method:

###### Default (synchronous) client
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

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new CloudConvertClient();

// File as input stream
final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file.jpg");

// Upload file using import/upload task
final TaskResponse uploadImportTaskResponse = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), inputStream).get().getBody().get().getData();

// Wait for import/upload task to be finished
final TaskResponse waitUploadImportTaskResponse = asyncCloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()).get().getBody().get().getData();
```

## Signing Webhook 
The node SDK allows to verify webhook requests received from CloudConvert.

###### Default (synchronous) client
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
final boolean isValid = cloudConvertClient.webhooks().verify(payload, signature);
```

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new AsyncCloudConvertClient();

// The JSON payload from the raw request body.
final String payload = "payload";

// The value of the "CloudConvert-Signature" header.
final String signature = "signature";

// You can find it in your webhook settings.
final String secret = "secret";

// Returns true if signature is valid, and false if signature is invalid
final boolean isValid = asyncCloudConvertClient.webhooks().verify(payload, signature);
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
