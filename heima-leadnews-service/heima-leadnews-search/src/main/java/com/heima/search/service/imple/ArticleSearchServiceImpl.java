package com.heima.search.service.imple;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {

    /**
     * es文章问也检索
     *
     * @param userSearchDto
     * @return
     */
    @Override
    public ResponseResult search(UserSearchDto userSearchDto) {
        // 检查参数

        //设置查询条件
        //关键字的文词之后查询

        //查询小于mindate的数据

        //按照发布时间倒叙查询

        //设置高亮title

        //结果封装返回
        return null;
    }
}
