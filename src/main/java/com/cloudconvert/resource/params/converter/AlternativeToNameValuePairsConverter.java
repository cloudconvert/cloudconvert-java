package com.cloudconvert.resource.params.converter;

import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AlternativeToNameValuePairsConverter implements Converter<Boolean, List<NameValuePair>> {

    private static final String PARAMETER_ALTERNATIVES = "alternatives";

    @Override
    public List<NameValuePair> convert(@Nullable final Boolean alternative) {
        return Optional.ofNullable(alternative).map(String::valueOf)
            .map(alternativeAsString -> new BasicNameValuePair(PARAMETER_ALTERNATIVES, alternativeAsString))
            .map(ImmutableList::<NameValuePair>of).orElse(ImmutableList.of());
    }
}
