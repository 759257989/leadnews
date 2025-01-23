package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件 然后上传到minIO中
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIo(ApArticle apArticle, String content);
}
