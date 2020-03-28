package com.cloudconvert.resource.params.converter;

import com.cloudconvert.resource.params.Pagination;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PaginationToNameValuePairsConverter implements Converter<Pagination, List<NameValuePair>> {

    private static final String PARAMETER_PER_PAGE = "per_page";
    private static final String PARAMETER_PAGE = "page";

    @Override
    public List<NameValuePair> convert(@Nullable final Pagination pagination) {
        return Optional.ofNullable(pagination)
            .map(paginationNotNull -> ImmutableList.<NameValuePair>of(
                new BasicNameValuePair(PARAMETER_PER_PAGE, String.valueOf(paginationNotNull.getPerPage())),
                new BasicNameValuePair(PARAMETER_PAGE, String.valueOf(paginationNotNull.getPage()))
            )).orElse(ImmutableList.of());
    }
}
