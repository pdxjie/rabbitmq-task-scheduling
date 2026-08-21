package com.taskflow.service;

import java.time.LocalDateTime;

/**
 * 任务日志服务接口
 */
public interface TaskLogService {

    /**
     * 记录任务开始
     */
    void logTaskStart(Long taskId, String traceId);

    /**
     * 记录任务成功
     */
    void logTaskSuccess(Long taskId, String traceId, LocalDateTime startTime, String result);

    /**
     * 记录任务失败
     */
    void logTaskFailure(Long taskId, String traceId, LocalDateTime startTime, Exception e);

    /**
     * 记录任务重试
     */
    void logTaskRetry(Long taskId, String traceId, int retryCount);
}
