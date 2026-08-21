package com.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskflow.entity.WorkflowDefinition;
import com.taskflow.workflow.model.WorkflowDAG;
import com.taskflow.workflow.model.WorkflowInstance;

/**
 * 工作流服务接口
 */
public interface WorkflowService extends IService<WorkflowDefinition> {

    /**
     * 创建工作流
     */
    WorkflowDefinition createWorkflow(WorkflowDAG dag, Long clusterId, String createdBy);

    /**
     * 更新工作流
     */
    WorkflowDefinition updateWorkflow(Long workflowId, WorkflowDAG dag);

    /**
     * 解析工作流定义
     */
    WorkflowDAG parseWorkflowDAG(WorkflowDefinition definition);

    /**
     * 执行工作流
     */
    WorkflowInstance executeWorkflow(Long workflowId);

    /**
     * 获取工作流执行实例
     */
    WorkflowInstance getWorkflowInstance(String instanceId);

    /**
     * 取消工作流执行
     */
    void cancelWorkflow(String instanceId);

    /**
     * 验证工作流 DAG
     */
    boolean validateWorkflow(WorkflowDAG dag);
}
