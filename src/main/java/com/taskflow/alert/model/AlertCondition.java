package com.taskflow.alert.model;

import lombok.Data;

/**
 * 告警条件
 */
@Data
public class AlertCondition {

    /**
     * 条件类型：TASK_FAIL/QUEUE_BACKLOG/CONSUMER_OFFLINE/EXECUTION_TIME
     */
    private String type;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 比较操作符：GT(>)/LT(<)/EQ(=)/GTE(>=)/LTE(<=)
     */
    private String operator;

    /**
     * 持续时间（秒）
     */
    private Integer duration;

    /**
     * 时间窗口（秒）
     */
    private Integer timeWindow;

    /**
     * 额外参数
     */
    private String params;
}
