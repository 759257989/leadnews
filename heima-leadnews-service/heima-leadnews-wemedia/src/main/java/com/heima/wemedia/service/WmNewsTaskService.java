package com.heima.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {

    /**
     * 加任务到延迟队列中
     * @param id 文章id
     * @param publishTime 文章中设定的发布时间，任务的执行时间
     */
    public void addNewstoTask(Integer id, Date publishTime);

    /**
     * 消费延迟队列数据
     */
    public void scanNewsByTask();
}
