package com.cloudconvert.dto.response;

import com.cloudconvert.dto.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class JobResponse extends Response {

    /**
     * The ID of the job.
     */
    private String id;

    /**
     * Your given tag of the job.
     */
    private String tag;

    /**
     * The status of the job. Is one of processing, finished or error .
     */
    private Status status;

    /**
     * ISO8601 timestamp of the creation of the job.
     */
    private String createdAt;

    /**
     * ISO8601 timestamp when the job started processing.
     */
    private String startedAt;

    /**
     * ISO8601 timestamp when the job finished or failed.
     */
    private String endedAt;

    /**
     * List of tasks that are part of the job.
     */
    private List<TaskResponse> tasks;

    /**
     * Link to self.
     */
    private Links links;


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Links {
        private String self;
    }
}
