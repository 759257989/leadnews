package com.heima.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service //service层接口注释
@Transactional
@Slf4j  //日志输出
public class ApArticleServiceImpl  extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    ApArticleMapper apArticleMapper;  //引入mapper接口依赖

    //默认一页最多显示50条文章
    private final static short MAX_PAGE_SIZE = 50;

    /**
     * 根据参数加载文章列表
     *
     * @param dto
     * @param type 1为加载更多  2为加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        //1 参数判断

        // 分页条数参数校验
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10; //如果没有指定文章数量，默认10篇文章
        }
        // 保证页面中不超过50篇文章
        size = Math.min(MAX_PAGE_SIZE, size);
        dto.setSize(size);


        //加载方式参数
        if (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE; //如果为声明加载方式，默认加载更多文章
        }

        //频道参数校验
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
            // 如果没有指定频道，默认所有频道加载
        }

        // 时间校验
        if(dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }
        //调用mapper层接口
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto,type);
        return ResponseResult.okResult(apArticles);
    }
}