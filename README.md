## cloudconvert-java

[![Tests](https://github.com/cloudconvert/cloudconvert-java/actions/workflows/run-tests.yml/badge.svg)](https://github.com/cloudconvert/cloudconvert-java/actions/workflows/run-tests.yml)
![Maven Central](https://img.shields.io/maven-central/v/com.cloudconvert/cloudconvert-java)

This is the official Java SDK v2 for the [CloudConvert](https://cloudconvert.com/api/v2) API.

## Installation
Add the following dependency to your pom.xml:
```
<dependency>
    <groupId>com.cloudconvert</groupId>
    <artifactId>cloudconvert-java</artifactId>
    <version>1.2.3</version>
</dependency>
```

## Creating API Client

###### Configuration
By default, API Key, Sandbox and Webhook Signing Secret are being read from `application.properties` file
```properties
CLOUDCONVERT_API_KEY=<api-key>
CLOUDCONVERT_SANDBOX=<true|false>
CLOUDCONVERT_WEBHOOK_SIGNING_SECRET=<secret>
```
It is also possible to provide configuration above using environment variables, custom properties file, system properties and string variables.
For all options, `CLOUDCONVERT_API_KEY`, `CLOUDCONVERT_SANDBOX` and `CLOUDCONVERT_WEBHOOK_SIGNING_SECRET` variable names should be used.

###### Default (synchronous) client
```java
// Using configuration from `application.properties` file
new CloudConvertClient();

// Using configuration from environment variables
new CloudConvertClient(new EnvironmentVariableSettingsProvider());

// Using configuration from custom properties file
new CloudConvertClient(new PropertyFileSettingsProvider("custom.properties"));

// Using configuration from string variables
new CloudConvertClient(new StringSettingsProvider("api-key", "webhook-signing-secret", false));

// Using configuration from system properties
new CloudConvertClient(new SystemPropertySettingsProvider()); 
```

###### Asynchronous client
```java
// Using configuration from `application.properties` file
new AsyncCloudConvertClient();

// Using configuration from environment variables
new AsyncCloudConvertClient(new EnvironmentVariableSettingsProvider());

// Using configuration from custom properties file
new AsyncCloudConvertClient(new PropertyFileSettingsProvider("custom.properties"));

// Using configuration from string variables
new AsyncCloudConvertClient(new StringSettingsProvider("api-key", "webhook-signing-secret", false));

// Using configuration from system properties
new AsyncCloudConvertClient(new SystemPropertySettingsProvider());
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
        "convert-my-file", new ConvertFilesTaskRequest()
                    .setInput("import-my-file")
                    .set("width", 100)
                    .set("height", 100),
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
    )
).getBody();

// Get a job id
final String jobId = createJobResponse.getId();

// Wait for a job completion
final JobResponse waitJobResponse = cloudConvertClient.jobs().wait(jobId).getBody();

// Get an export/url task id
final String exportUrlTaskId = waitJobResponse.getTasks().stream().filter(taskResponse -> taskResponse.getName().equals("export-my-file")).findFirst().get().getId();
```

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new AsyncCloudConvertClient();

// Create a job
final JobResponse createJobResponse = asyncCloudConvertClient.jobs().create(
    ImmutableMap.of(
        "import-my-file", new UrlImportRequest().setUrl("import-url"),
        "convert-my-file", new ConvertFilesTaskRequest()
                    .setInput("import-my-file")
                    .set("width", 100)
                    .set("height", 100),
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
    )
).get().getBody();

// Get a job id
final String jobId = createJobResponse.getId();

// Wait for a job completion
final JobResponse waitJobResponse = asyncCloudConvertClient.jobs().wait(jobId).get().getBody();

// Get an export/url task id
final String exportUrlTaskId = waitJobResponse.getTasks().stream().filter(taskResponse -> taskResponse.getName().equals("export-my-file")).findFirst().get().getId();
```

## Downloading Files
CloudConvert can generate public URLs using `export/url` tasks. You can use these URLs to download output files.

###### Default (synchronous) client
```java
// Wait for an export/url task to be finished
final TaskResponse waitUrlExportTaskResponse = cloudConvertClient.tasks().wait(exportUrlTaskId).getBody();

// Get url and filename of export/url task
final String exportUrl = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url");
final String filename = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("filename");

// Get file as input stream using url of export/url task
final InputStream inputStream = cloudConvertClient.files().download(exportUrl).getBody();

// Save to local file
OutputStream outputStream = new FileOutputStream(new File(filename));
IOUtils.copy(inputStream, outputStream);
```

###### Asynchronous client
```java
// Wait for an export/url task to be finished
final TaskResponse waitUrlExportTaskResponse = asyncCloudConvertClient.tasks().wait(exportUrlTaskId).get().getBody();

// Get a url of export/url task
final String exportUrl = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("url");
final String filename = waitUrlExportTaskResponse.getResult().getFiles().get(0).get("filename");

// Get file as input stream using url of export/url task
final InputStream inputStream = asyncCloudConvertClient.files().download(exportUrl).get().getBody();

// Save to local file
OutputStream outputStream = new FileOutputStream(new File(filename));
IOUtils.copy(inputStream, outputStream);
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
final TaskResponse uploadImportTaskResponse = cloudConvertClient.importUsing().upload(new UploadImportRequest(), inputStream, "file.jpg").getBody();

// Wait for import/upload task to be finished
final TaskResponse waitUploadImportTaskResponse = cloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()).getBody();
```

###### Asynchronous client
```java
// Create a client
final AsyncCloudConvertClient asyncCloudConvertClient = new CloudConvertClient();

// File as input stream
final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file.jpg");

// Upload file using import/upload task
final TaskResponse uploadImportTaskResponse = asyncCloudConvertClient.importUsing().upload(new UploadImportRequest(), inputStream, "file.jpg").get().getBody();

// Wait for import/upload task to be finished
final TaskResponse waitUploadImportTaskResponse = asyncCloudConvertClient.tasks().wait(uploadImportTaskResponse.getId()).get().getBody();
```

## Verify Webhook Signatures
The node SDK allows to verify webhook requests received from CloudConvert.

```java
// Create a client
final CloudConvertClient cloudConvertClient = new CloudConvertClient();

// The JSON payload from the raw request body.
final String payload = "payload";

// The value of the "CloudConvert-Signature" header.
final String signature = "signature";

// Returns true if signature is valid, and false if signature is invalid
final boolean isValid = cloudConvertClient.webhooks().verify(payload, signature);
```

## Signed URLs

Signed URLs allow converting files on demand only using URL query parameters. The Java SDK allows to generate such URLs. Therefore, you need to obtain a signed URL base and a signing secret on the [CloudConvert Dashboard](https://cloudconvert.com/dashboard/api/v2/signed-urls).

```java

final String base = "https://s.cloudconvert.com/..."; // You can find it in your signed URL settings.
final String signingSecret = "..."; // You can find it in your signed URL settings.
final String cacheKey = "mykey"; // Allows caching of the result file for 24h

final Map<String, TaskRequest> tasks = ImmutableMap.of(
        "import-my-file", new UrlImportRequest().setUrl("import-url"),
        "convert-my-file", new ConvertFilesTaskRequest()
        .setInput("import-my-file")
        .setOutputFormat("pdf")
        "export-my-file", new UrlExportRequest().setInput("convert-my-file")
        );


final String url = cloudConvertClient.signedUrls().sign(base, signingSecret, tasks, cacheKey);
```

## Unit Tests
```
$ mvn clean install -U -Punit-tests
```

## Integration Tests
```
$ mvn clean install -U -Pintegration-tests
```
       
By default, this runs the integration tests against the Sandbox API with an official CloudConvert account. If you would like to use your own account, you can set your API key in the `application.properties` file. In this case you need to whitelist the following MD5 hashes for Sandbox API (using the CloudConvert dashboard).

    07db6477193bf8313e8082a1e1b5eaf6  image-test-file-1.jpg
    7ef166ecc65949f6f2e7eb94a3dac0d4  image-test-file-2.jpg
    ccbb000ef5bd9dad0fab600d2fff02fb  odt-test-file-1.odt
    3a3b4d07338b51db19056a73a89a186b  odt-test-file-2.odt
       
## Resources
* [API v2 Documentation](https://cloudconvert.com/api/v2)
* [CloudConvert Blog](https://cloudconvert.com/blog)
