package com.cloudconvert.dto.response;

import com.cloudconvert.dto.Operation;
import com.cloudconvert.dto.Status;
import com.cloudconvert.dto.request.TaskRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class TaskResponse extends Response {

    /**
     * The ID of the task.
     */
    private String id;

    /**
     * Your given name of the task.
     */
    private String name;

    /**
     * The Job ID the tasks belongs to.
     */
    private String jobId;

    /**
     * Name of the operation, for example convert or import/s3.
     */
    private Operation operation;

    /**
     * The status of the task. Is one of waiting, processing, finished or error.
     */
    private Status status;

    /**
     * The status message. Contains the error message if the task status is error.
     */
    private String message;

    /**
     * The error code if the task status is error.
     */
    private String code;

    /**
     * The amount of conversion minutes the task consumed. Available when the status is finished.
     */
    private Integer credits;

    /**
     * ISO8601 timestamp of the creation of the task.
     */
    private String createdAt;

    /**
     * ISO8601 timestamp when the task started processing.
     */
    private String startedAt;

    /**
     * ISO8601 timestamp when the task finished or failed.
     */
    private String endedAt;

    /**
     * List of tasks that are dependencies for this task. Only available if the include parameter was set to depends_on_tasks.
     */
    private List<String> dependsOnTaskIds;

    /**
     * ID of the original task, if this task is a retry.
     */
    private String retryOfTaskId;

    /**
     * List of tasks that are retries of this task. Only available if the include parameter was set to retries.
     */
    private List<String> retries;

    /**
     * Name of the engine.
     */
    private String engine;

    /**
     * Version of the engine.
     */
    private String engineVersion;

    private String userId;

    private Integer priority;

    private String storage;

    /**
     * Your submitted payload for the tasks. Depends on the operation type.
     */
    private TaskRequest payload;

    /**
     * The result of the task. Depends on the operation type.
     * Finished tasks always do have a files key with the names of the result files of the task (See the example on the right).
     */
    private Result result;

    /**
     * Link to self.
     */
    private Links links;


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Result {

        private List<Map<String, String>> files;

        private Form form;


        @Getter
        @Setter
        @Accessors(chain = true)
        @ToString
        @EqualsAndHashCode
        public static class Form {

            private String url;

            private Map<String, String> parameters;
        }
    }


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Links {

        private String self;
    }
}
