package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {
    //注入service层接口
    @Autowired
    private ApArticleService apArticleService;

    /**
     * 首页加载controller
     * @param dto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载更多controller
     * @param dto
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载更新的文章controller
     * @param dto
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }
}