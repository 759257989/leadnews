package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.WmSensitive;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    /**
     * 自媒体审核
     */
    @Async//异步调用
    @Override
    public void autoScanWmNews(Integer id) {
        log.debug("Starting autoScanWmNews with ID: {}", id);
        //1 查询自媒体文章
//        WmNews wmNews = null;
//
//        for (int i = 0; i < 5; i++) { // 重试5次
//            wmNews = wmNewsMapper.selectById(id);
//            if (wmNews != null) break;
//            try {
//                Thread.sleep(500); // 每次延迟500ms
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException("Thread interrupted", e);
//            }
//        }
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl - 错误无法审核 文章不存在");
        }

        log.debug("Retrieved WmNews: {}", wmNews);
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            // 处理待审核的文章

            //内容中提取文本，图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            //敏感词过滤
            boolean isSensitice = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if (!isSensitice) {
                return; //存在敏感词，不通过
            }
            //2审核文本内容
            boolean isTextScan = handleTextScan(textAndImages.get("content").toString(), wmNews);
            if(!isTextScan) return; //审核不通过
            //3审核图片内容
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
            if(!isImageScan) return;
        }
        //4审核成功，保存到app端 文章相关数据
        saveAppArticle(wmNews);
    }

    @Autowired
    WmSensitiveMapper wmSensitiveMapper;
    /**
     * 敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;
        //1获取所有敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //2初始化词库
        SensitiveWordUtil.initMap(sensitiveList);

        //检查文章中是否存在敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if (map.size() > 0){
            updateWmNews(wmNews, (short)2, "当前文章中存在违规内容" + map);
            flag = false;
        }
        return flag;
    }

    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    /**
     * 保存app端相关的文章数据
     * @param wmNews
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        //属性拷贝
        BeanUtils.copyProperties(wmNews, dto);
        //文章的布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel != null) {
            dto.setChannelName(wmChannel.getName());
        }
        //作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            dto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if(wmNews.getArticleId() != null) {
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        //审核通过，保存到app端
        ResponseResult responseResult = articleClient.saveArticle(dto);
        if (!responseResult.getCode().equals(200)){
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
        }
        //回填article id
        wmNews.setArticleId((long) responseResult.getData());
        //修改文章状态成已发布
        updateWmNews(wmNews,(short) 9, "成功审核");

        return responseResult;
    }

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GreenImageScan greenImageScan;

    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;

        if(images == null || images.size() == 0){
            return flag;
        }
        //下载图片 minIO
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());

        List<byte[]> imageList = new ArrayList<>();

        for (String image : images) {
            byte[] bytes = fileStorageService.downLoadFile(image);
            imageList.add(bytes);
        }
        //审核图片
        try {
            Map map = MyScan();
//            Map map = greenImageScan.imageScan(imageList);
            if(map != null){
                //审核失败
                if(map.get("suggestion").equals("block")){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }
                //不确定信息  需要人工审核
                if(map.get("suggestion").equals("review")){
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                }
            }

        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    private Map MyScan() {
        Map<String, String> map = new HashMap<>();
        double random = Math.random(); // random value between 0.0 and 1.0

        if (random < 0.015) { // 15% chance for "block"
            map.put("suggestion", "block");
        } else if (random < 0.030) { // 15% chance for "review"
            map.put("suggestion", "review");
        } else { // 70% chance for passing
            map.put("suggestion", "pass");
        }
        return map;
    }

    @Autowired
    private GreenTextScan greenTextScan;
    /**
     * 审核文本内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;

        if((wmNews.getTitle()+""+content).length() == 0){
            return flag; //空标题，或者空文本
        }
        try {
            Map map = MyScan();
//            Map map = greenTextScan.greeTextScan((wmNews.getTitle()+"-"+content));
            if(map != null){
                //审核失败
                if(map.get("suggestion").equals("block")){
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
                }

                //不确定信息  需要人工审核
                if(map.get("suggestion").equals("review")){
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "当前文章中存在不确定内容");
                }
            }

        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改文章内容
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }
    /**
     * 从文章中提取文章文本和文本中的照片
     * 从文章中提取封面照片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        //存储文章文本内容
        StringBuilder stringBuilder = new StringBuilder();

        //存储文章图片
        List<String> images = new ArrayList<>();

        if (StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> contents = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for(Map content : contents){
                if(content.get("type").equals("image")){
                    images.add((String) content.get("value"));
                }
                if (content.get("type").equals("text")){
                    stringBuilder.append(content.get("value"));
                }
            }
        }
        // 文章封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("content", stringBuilder.toString());
        result.put("images", images);
        return result;
    }
}
