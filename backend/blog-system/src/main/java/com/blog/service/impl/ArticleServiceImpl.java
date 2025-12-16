package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Override
    public List<Article> getArticles(int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findList(offset, size);
    }
    
    @Override
    public Article getArticleById(Integer id) {
        // 先获取文章
        Article article = articleMapper.findById(id);
        
        // 增加阅读量
        if (article != null) {
            articleMapper.incrementViewCount(id);
            // 重新获取更新后的数据
            article = articleMapper.findById(id);
        }
        
        return article;
    }
    
    @Override
    public boolean createArticle(Article article) {
        int result = articleMapper.insert(article);
        return result > 0;
    }
    
    @Override
    public boolean updateArticle(Article article) {
        int result = articleMapper.update(article);
        return result > 0;
    }
    
    @Override
    public boolean deleteArticle(Integer id) {
        // 这里使用软删除，将状态设为2
        Article article = new Article();
        article.setId(id);
        article.setStatus(2); // 删除状态
        int result = articleMapper.update(article);
        return result > 0;
    }
    
    @Override
    public void incrementViewCount(Integer id) {
        articleMapper.incrementViewCount(id);
    }
}