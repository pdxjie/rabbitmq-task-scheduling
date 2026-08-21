package com.taskflow.workflow.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作流 DAG 定义
 */
@Data
public class WorkflowDAG {

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 节点列表
     */
    private List<WorkflowNode> nodes = new ArrayList<>();

    /**
     * 边（依赖关系）列表
     */
    private List<WorkflowEdge> edges = new ArrayList<>();

    /**
     * 全局参数
     */
    private String globalParams;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    @Data
    public static class WorkflowEdge {
        /**
         * 源节点ID
         */
        private String source;

        /**
         * 目标节点ID
         */
        private String target;

        /**
         * 条件：SUCCESS/FAILED/ALWAYS
         */
        private String condition = "SUCCESS";
    }
}
