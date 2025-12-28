package com.blog.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer size;
    private List<T> data;
    private Integer totalPages;
    
    public PageResult(Long total, Integer page, Integer size, List<T> data) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.data = data;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}