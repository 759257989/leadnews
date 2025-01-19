package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    /**
     * 查询文章
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        // 1 检查参数
        dto.checkParam();

        // 2 分页查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 4 状态查询条件设置
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        // 5 频道查询条件设置
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }

        // 时间范围查询条件设置
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }

        // 关键字查询条件设置
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }

        // 登陆人查询条设置
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());

        //发布时间倒序排序条件设置
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);

        page = page(page, lambdaQueryWrapper);

        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int)page.getTotal());

        responseResult.setData(page.getRecords());
        return responseResult;
    }
}
