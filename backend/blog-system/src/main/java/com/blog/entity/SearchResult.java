package com.blog.entity;

import lombok.Data;
import java.util.List;

@Data
public class SearchResult<T> {
    private String keyword;           // 搜索关键词
    private Integer total;            // 总结果数
    private Integer page;             // 当前页码
    private Integer size;             // 每页大小
    private List<T> items;           // 搜索结果列表
    
    public SearchResult() {}
    
    public SearchResult(String keyword, Integer total, Integer page, 
                       Integer size, List<T> items) {
        this.keyword = keyword;
        this.total = total;
        this.page = page;
        this.size = size;
        this.items = items;
    }
}