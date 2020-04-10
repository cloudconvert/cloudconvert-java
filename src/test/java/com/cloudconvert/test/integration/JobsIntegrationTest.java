package com.cloudconvert.test.integration;

import com.cloudconvert.client.CloudConvertClient;
import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.MergeFilesTaskRequest;
import com.cloudconvert.dto.request.TaskRequest;
import com.cloudconvert.dto.request.UploadImportRequest;
import com.cloudconvert.dto.response.JobResponse;
import com.cloudconvert.dto.response.Pageable;
import com.cloudconvert.dto.response.TaskResponse;
import com.cloudconvert.dto.result.Result;
import com.cloudconvert.resource.params.Pagination;
import com.cloudconvert.test.framework.AbstractTest;
import com.cloudconvert.test.framework.IntegrationTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(JUnit4.class)
public class JobsIntegrationTest extends AbstractTest {

    private static final String PDF = "pdf";

    private static final String ODT_TEST_FILE_1 = "odt-test-file-1.odt";
    private static final String ODT_TEST_FILE_2 = "odt-test-file-2.odt";

    private CloudConvertClient cloudConvertClient;

    private InputStream odtTest1InputStream;

    private InputStream odtTest2InputStream;

    @Before
    public void before() throws Exception {
        cloudConvertClient = new CloudConvertClient();

        odtTest1InputStream = JobsIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_1);
        odtTest2InputStream = JobsIntegrationTest.class.getClassLoader().getResourceAsStream(ODT_TEST_FILE_2);
    }

    /**
     * There are few restrictions from the API:
     * 1. Conversion, which has multiple input files is allowed within jobs only;
     * 2. Merge requires at least 2 input files to be passed
     * <p>
     * So this integration test tests job lifecycle, which contains (upload + upload + merge + export url) tasks then delete the job
     */
    @Test(timeout = TIMEOUT)
    public void mergeFileTaskAsJobLifecycle() throws Exception {
        final String uploadFile1TaskName = "import-image-test-file-1";
        final String uploadFile2TaskName = "import-image-test-file-2";
        final String mergeFile1AndFile2TaskName = "image-test-file-1-and-import-image-test-file-2-merge";

        final Map<String, TaskRequest> tasks = ImmutableMap.of(
            uploadFile1TaskName, new UploadImportRequest(),
            uploadFile2TaskName, new UploadImportRequest(),
            mergeFile1AndFile2TaskName, new MergeFilesTaskRequest().setOutputFormat(PDF).setInput("import-image-test-file-1", "import-image-test-file-2")
        );

        // Create job
        final Result<JobResponse> jobResponseResult = cloudConvertClient.jobs().create(tasks);
        assertThat(jobResponseResult.getStatus()).isEqualTo(HttpStatus.SC_CREATED);

        final JobResponse jobResponse = jobResponseResult.getBody();
        assertThat(jobResponse.getTasks()).hasSize(3);
        assertThat(jobResponse.getStatus()).isEqualTo(Status.WAITING);
        assertThat(jobResponse.getTasks()).extracting(TaskResponse::getName).contains(uploadFile1TaskName, uploadFile2TaskName, mergeFile1AndFile2TaskName);

        final TaskResponse uploadFile1TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(uploadFile1TaskName)).findFirst().get();
        final TaskResponse uploadFile2TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(uploadFile2TaskName)).findFirst().get();
        final TaskResponse mergeFile1AndFile2TaskJobResponse = jobResponse.getTasks().stream()
            .filter(taskResponse -> taskResponse.getName().equals(mergeFile1AndFile2TaskName)).findFirst().get();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds()).isNotEmpty();
        assertThat(mergeFile1AndFile2TaskJobResponse.getDependsOnTaskIds()).contains(uploadFile1TaskJobResponse.getId(), uploadFile2TaskJobResponse.getId());

        // Upload (actual upload file 1)
        final Result<TaskResponse> uploadFile1TaskResponseResult = cloudConvertClient.importUsing()
            .upload(uploadFile1TaskJobResponse.getId(), uploadFile1TaskJobResponse.getResult().getForm(), odtTest1InputStream);
        assertThat(uploadFile1TaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile1TaskResponse = uploadFile1TaskResponseResult.getBody();
        assertThat(uploadFile1TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Upload (actual upload file 2)
        final Result<TaskResponse> uploadFile2TaskResponseResult = cloudConvertClient.importUsing()
            .upload(uploadFile2TaskJobResponse.getId(), uploadFile2TaskJobResponse.getResult().getForm(), odtTest2InputStream);
        assertThat(uploadFile2TaskResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final TaskResponse uploadFile2TaskResponse = uploadFile2TaskResponseResult.getBody();
        assertThat(uploadFile2TaskResponse.getOperation()).isEqualTo(Operation.IMPORT_UPLOAD);

        // Wait
        final Result<JobResponse> waitJobResponseResult = cloudConvertClient.jobs().wait(jobResponse.getId());
        assertThat(waitJobResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse waitJobResponse = waitJobResponseResult.getBody();
        assertThat(waitJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(waitJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Show
        final Result<JobResponse> showJobResponseResult = cloudConvertClient.jobs().show(jobResponse.getId());
        assertThat(showJobResponseResult.getStatus()).isEqualTo(HttpStatus.SC_OK);

        final JobResponse showJobResponse = showJobResponseResult.getBody();
        assertThat(showJobResponse.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(showJobResponse.getId()).isEqualTo(jobResponse.getId());

        // Delete job
        final Result<Void> deleteVoidResult = cloudConvertClient.jobs().delete(jobResponse.getId());
        assertThat(deleteVoidResult.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
    }

    @Test(timeout = TIMEOUT)
    public void listJobsLifecycle() throws Exception {
        // List jobs
        final Result<Pageable<JobResponse>> jobResponsePageable = cloudConvertClient.jobs().list(ImmutableMap.of(), ImmutableList.of(), new Pagination(10, 1));
        assertThat(jobResponsePageable.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @After
    public void after() throws Exception {
        odtTest1InputStream.close();
        odtTest2InputStream.close();
        cloudConvertClient.close();
    }
}
