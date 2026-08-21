package com.taskflow.workflow.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流执行实例
 */
@Data
public class WorkflowInstance {

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 执行状态：RUNNING/SUCCESS/FAILED/CANCELLED
     */
    private String status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 节点执行状态（节点ID -> 状态）
     */
    private Map<String, NodeExecutionStatus> nodeStatuses = new HashMap<>();

    /**
     * 执行参数
     */
    private String executionParams;

    /**
     * 错误信息
     */
    private String errorMessage;

    @Data
    public static class NodeExecutionStatus {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
         */
        private String status;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 执行结果
         */
        private String result;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 重试次数
         */
        private Integer retryCount = 0;
    }
}
