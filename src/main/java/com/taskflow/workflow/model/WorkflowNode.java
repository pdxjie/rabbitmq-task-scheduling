package com.taskflow.workflow.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作流节点
 */
@Data
public class WorkflowNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：TASK/CONDITION/PARALLEL/AGGREGATE
     */
    private String nodeType;

    /**
     * 关联的任务ID（TASK类型）
     */
    private Long taskId;

    /**
     * 条件表达式（CONDITION类型）
     */
    private String condition;

    /**
     * 依赖的节点ID列表
     */
    private List<String> dependencies = new ArrayList<>();

    /**
     * 节点配置参数
     */
    private String nodeParams;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 是否允许失败
     */
    private Boolean allowFailure = false;

    /**
     * 节点位置（UI用）
     */
    private Position position;

    @Data
    public static class Position {
        private Integer x;
        private Integer y;
    }
}
