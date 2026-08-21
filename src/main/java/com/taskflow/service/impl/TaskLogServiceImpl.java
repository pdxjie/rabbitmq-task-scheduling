package com.taskflow.service.impl;

import com.taskflow.entity.TaskExecutionLog;
import com.taskflow.mapper.TaskExecutionLogMapper;
import com.taskflow.service.TaskDefinitionService;
import com.taskflow.service.TaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 任务日志服务实现
 */
@Slf4j
@Service
public class TaskLogServiceImpl implements TaskLogService {

    private final TaskExecutionLogMapper logMapper;
    private final TaskDefinitionService taskDefinitionService;

    public TaskLogServiceImpl(TaskExecutionLogMapper logMapper,
                              @Lazy TaskDefinitionService taskDefinitionService) {
        this.logMapper = logMapper;
        this.taskDefinitionService = taskDefinitionService;
    }

    @Override
    public void logTaskStart(Long taskId, String traceId) {
        TaskExecutionLog log = new TaskExecutionLog();
        log.setTaskId(taskId);
        log.setTraceId(traceId);
        log.setStartTime(LocalDateTime.now());
        log.setStatus("RUNNING");
        log.setRetryCount(0);

        logMapper.insert(log);
    }

    @Override
    public void logTaskSuccess(Long taskId, String traceId, LocalDateTime startTime, String result) {
        TaskExecutionLog log = new TaskExecutionLog();
        log.setTaskId(taskId);
        log.setTraceId(traceId);
        log.setStartTime(startTime);
        log.setEndTime(LocalDateTime.now());
        log.setDurationMs(Duration.between(startTime, LocalDateTime.now()).toMillis());
        log.setStatus("SUCCESS");
        log.setResult(result);

        logMapper.insert(log);

        // 更新任务统计
        taskDefinitionService.updateTaskStats(taskId, true);
    }

    @Override
    public void logTaskFailure(Long taskId, String traceId, LocalDateTime startTime, Exception e) {
        TaskExecutionLog log = new TaskExecutionLog();
        log.setTaskId(taskId);
        log.setTraceId(traceId);
        log.setStartTime(startTime);
        log.setEndTime(LocalDateTime.now());
        log.setDurationMs(Duration.between(startTime, LocalDateTime.now()).toMillis());
        log.setStatus("FAILED");
        log.setErrorMessage(e.getMessage());
        log.setErrorStack(getStackTrace(e));

        logMapper.insert(log);

        // 更新任务统计
        taskDefinitionService.updateTaskStats(taskId, false);
    }

    @Override
    public void logTaskRetry(Long taskId, String traceId, int retryCount) {
        TaskExecutionLog log = new TaskExecutionLog();
        log.setTaskId(taskId);
        log.setTraceId(traceId);
        log.setStartTime(LocalDateTime.now());
        log.setStatus("RETRY");
        log.setRetryCount(retryCount);

        logMapper.insert(log);
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
