package com.heima.schedule.service.Impl;

import com.heima.common.constants.ScheduleConstants;
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

        //2.1如果任务的执行时间小于等于当前时间，存入list

        //2.2 如果任务的执行时间大于当前时间 && 小于预设时间 5分钟，存入zset中
        return 0;
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
}
