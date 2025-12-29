package com.blog.entity;

import lombok.Data;
import java.util.List;

@Data
public class FullSearchResponse {
    private String keyword;
    private Integer page;
    private Integer size;
    private Integer total;
    private List<Article> articles;
    private List<User> users;
    private List<Tag> tags;
    
    public FullSearchResponse(String keyword, Integer page, Integer size, Integer total,
                             List<Article> articles, List<User> users, List<Tag> tags) {
        this.keyword = keyword;
        this.page = page;
        this.size = size;
        this.total = total;
        this.articles = articles;
        this.users = users;
        this.tags = tags;
    }
}