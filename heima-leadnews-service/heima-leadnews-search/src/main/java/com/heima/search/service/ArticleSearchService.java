package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface ArticleSearchService {

    /**
     * es文章问也检索
     * @param userSearchDto
     * @return
     */
    public ResponseResult search( UserSearchDto userSearchDto);
}
