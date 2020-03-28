package com.cloudconvert.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserResponse extends Response {

    /**
     * The ID of the user.
     */
    private String id;

    /**
     * The username of the user.
     */
    private String username;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * Remaining conversion minutes.
     */
    private Integer credits;

    /**
     * ISO8601 timestamp when the user was created.
     */
    private String createdAt;
}
