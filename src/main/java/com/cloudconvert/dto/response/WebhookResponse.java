package com.cloudconvert.dto.response;

import com.cloudconvert.dto.Event;
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
public class WebhookResponse extends Response {

    private String id;

    private String url;

    private Boolean disabled;

    private List<Event> events;

    private Boolean failing;

    private String signingSecret;

    private String createdAt;

    private String updatedAt;

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
