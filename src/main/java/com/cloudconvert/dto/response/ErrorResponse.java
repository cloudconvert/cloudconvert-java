package com.cloudconvert.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class ErrorResponse {

    private String code;

    private String message;

    @Nullable
    private Map<String, Object> errors;
}
