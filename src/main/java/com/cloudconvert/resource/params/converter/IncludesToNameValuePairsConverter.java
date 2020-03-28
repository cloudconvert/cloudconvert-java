package com.cloudconvert.resource.params.converter;

import com.cloudconvert.resource.params.Include;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IncludesToNameValuePairsConverter implements Converter<List<Include>, List<NameValuePair>> {

    private static final String PARAMETER_INCLUDE = "include";

    @Override
    public List<NameValuePair> convert(@NotNull final List<Include> includes) {
        return includes.stream().map(Include::getLabel).reduce((include1, include2) -> include1 + "," + include2)
            .map(includesAsString -> new BasicNameValuePair(PARAMETER_INCLUDE, includesAsString)).map(ImmutableList::<NameValuePair>of).orElse(ImmutableList.of());
    }
}
