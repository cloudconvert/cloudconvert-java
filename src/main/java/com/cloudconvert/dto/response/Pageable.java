package com.cloudconvert.dto.response;

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
@EqualsAndHashCode
public class Pageable<T> {

    private List<T> data;
    private Links links;
    private Meta meta;


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Links {

        private String first;
        private String last;
        private String prev;
        private String next;
    }


    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class Meta {

        private Integer currentPage;
        private Integer from;
        private Integer to;
        private String path;
        private Integer perPage;
    }
}
