package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class TaskRequest extends Request {

    /**
     * Conversion and engine specific options. Depends on input_format and output_format.
     */
    @JsonIgnore
    private Map<String, String> properties;

    /**
     * Set specific option
     *
     * @param key   key
     * @param value value. If null, then specific option is removed
     * @return {@link TaskRequest}
     */
    public TaskRequest set(@NotNull final String key, @Nullable final String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }

        return this;
    }

    /**
     * Get specific option
     *
     * @param key key
     * @return specific option value
     */
    public String get(@NotNull final String key) {
        return properties.get(key);
    }

    public abstract Operation getOperation();
}
