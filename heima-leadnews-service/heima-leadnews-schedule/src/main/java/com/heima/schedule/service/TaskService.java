package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

public interface TaskService {

    /**
     * 添加延迟任务
     * @param task
     * @return 返回当前任务的id
     */
    public long addTask(Task task);

    /**
     * 删除任务/取消任务
     * @param id
     * @return
     */
    public boolean cancelTask(long id);
}
