package com.cloudconvert.resource.params;

import lombok.Getter;

@Getter
public class Pagination {

    private final int perPage;
    private final int page;

    public Pagination(final int perPage, final int page) {
        this.perPage = perPage;
        this.page = page;
    }
}
