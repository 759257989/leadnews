package com.heima.schedule.service.Impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    /**
     * 添加延迟任务
     *
     * @param task
     * @return 返回当前任务的id
     */
    @Override
    public long addTask(Task task) {

        //1添加任务到数据库中
        boolean success = addTaskToDB(task);
        //2添加任务到redis中

        if(success){
            addTaskToCache(task);
        }

        return 0;
    }

    @Autowired
    private CacheService cacheService;
    /**
     * 把任务添加到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        //获取5分钟之后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long next5ScheduleTime = calendar.getTimeInMillis();

        //2.1如果任务的执行时间小于等于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC+ key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= next5ScheduleTime) {
            //2.2 如果任务的执行时间大于当前时间 && 小于预设时间 5分钟，存入zset中
            cacheService.zAdd(ScheduleConstants.FUTURE+key, JSON.toJSONString(task), task.getExecuteTime()); //key, task, score
        }
    }


    @Autowired
    TaskinfoMapper taskinfoMapper;
    @Autowired
    TaskinfoLogsMapper taskinfoLogsMapper;
    //添加任务到数据库中
    private boolean addTaskToDB(Task task) {
        boolean flag = false;
        try {
            //保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            // 设置taskID
            task.setTaskId(taskinfo.getTaskId());
            //保存任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除任务/取消任务
     *
     * @param id
     * @return
     */
    @Override
    public boolean cancelTask(long id) {
        boolean flag = false;
        //1 删除db中的任务，更新任务日志
        Task task = updateDb(id, ScheduleConstants.CANCELLED);
        //2 删除redis数据
        if(task != null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    //删除redis中的数据
    private void removeTaskFromCache(Task task) {
        //根据key查询到匹配的数据进行删除
        String key = task.getTaskType() + "_" + task.getPriority();

        // 根据任务执行时间进行对应的数据表中删除
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC+key, 0, JSON.toJSONString(task));
        } else{
            cacheService.zRemove(ScheduleConstants.FUTURE+key, 0, JSON.toJSONString(task));
        }
    }


    //删除任务，更新日志
    private Task updateDb(long id, int cancelled) {
        Task task = null;

        try {
            //删除
            taskinfoMapper.deleteById(id);
            //更新日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(id);
            taskinfoLogs.setStatus(cancelled); //修改数据
            taskinfoLogsMapper.updateById(taskinfoLogs); //重新更新到库中

            //返回参数，修改过的task
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("task cancel exception taskID = {}", id);
        }

        return task;
    }

}
