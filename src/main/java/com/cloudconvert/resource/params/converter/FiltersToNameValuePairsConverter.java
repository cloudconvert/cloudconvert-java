package com.cloudconvert.resource.params.converter;

import com.cloudconvert.resource.params.Filter;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FiltersToNameValuePairsConverter implements Converter<Map<Filter, String>, List<NameValuePair>> {

    private static final String PARAMETER_FILTER = "filter";

    @Override
    public List<NameValuePair> convert(@NotNull final Map<Filter, String> filters) {
        return filters.entrySet().stream()
            .map(entry -> new BasicNameValuePair(PARAMETER_FILTER + "[" + entry.getKey().getLabel() + "]", entry.getValue())).collect(Collectors.toList());
    }
}
