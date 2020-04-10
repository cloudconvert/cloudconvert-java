package com.cloudconvert.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class Request {

    /**
     * Conversion and engine specific options. Depends on input_format and output_format.
     */
    @Getter
    @JsonIgnore
    private final Map<String, Object> properties;

    public Request() {
        properties = new HashMap<>();
    }

    /**
     * Set specific option
     *
     * @param key   key
     * @param value value. If null, then specific option is removed
     * @return {@link TaskRequest}
     */
    public <T extends Request> T setProperty(@NotNull final String key, @Nullable final Object value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }

        return (T) this;
    }

    /**
     * Set specific option. Shortcut for {@link #setProperty(String, Object)}
     *
     * @param key   key
     * @param value value. If null, then specific option is removed
     * @return {@link TaskRequest}
     */
    public <T extends Request> T set(@NotNull final String key, @Nullable final Object value) {
        return setProperty(key, value);
    }

    /**
     * Get specific option
     *
     * @param key key
     * @return specific option value
     */
    public Object getProperty(@NotNull final String key) {
        return properties.get(key);
    }

    /**
     * Get specific option. Shortcut for {@link #getProperties()}
     *
     * @param key key
     * @return specific option value
     */
    public Object get(@NotNull final String key) {
        return properties.get(key);
    }
}
