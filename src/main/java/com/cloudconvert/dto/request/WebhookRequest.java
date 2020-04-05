package com.cloudconvert.dto.request;

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
public class WebhookRequest extends Request {

    /**
     * (required) The URL to send the notifications to.
     */
    private String url;

    /**
     * (required)  Select the events.
     */
    private List<Event> events;
}
