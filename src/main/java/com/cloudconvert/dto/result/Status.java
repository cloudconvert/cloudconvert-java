package com.cloudconvert.dto.result;

import lombok.Builder;
import lombok.Getter;

@Builder
public class Status {

    @Getter
    private int code;

    @Getter
    private String reason;

    public boolean isInformational() {
        return 100 <= code && code <= 199;
    }

    public boolean isSuccessful() {
        return 200 <= code && code <= 299;
    }

    public boolean isRedirection() {
        return 300 <= code && code <= 399;
    }

    public boolean isClientError() {
        return 400 <= code && code <= 499;
    }

    public boolean isServerError() {
        return 500 <= code && code <= 599;
    }
}
