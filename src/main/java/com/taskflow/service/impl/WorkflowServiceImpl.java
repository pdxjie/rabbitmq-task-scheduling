package com.taskflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.entity.WorkflowDefinition;
import com.taskflow.entity.WorkflowExecutionLog;
import com.taskflow.mapper.WorkflowDefinitionMapper;
import com.taskflow.mapper.WorkflowExecutionLogMapper;
import com.taskflow.service.WorkflowService;
import com.taskflow.workflow.engine.DAGExecutor;
import com.taskflow.workflow.engine.DAGValidator;
import com.taskflow.workflow.model.WorkflowDAG;
import com.taskflow.workflow.model.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 工作流服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl extends ServiceImpl<WorkflowDefinitionMapper, WorkflowDefinition>
        implements WorkflowService {

    private final DAGValidator dagValidator;
    private final DAGExecutor dagExecutor;
    private final WorkflowExecutionLogMapper executionLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition createWorkflow(WorkflowDAG dag, Long clusterId, String createdBy) {
        // 验证 DAG
        if (!dagValidator.validateDAG(dag)) {
            throw new RuntimeException("工作流 DAG 验证失败");
        }

        try {
            WorkflowDefinition definition = new WorkflowDefinition();
            definition.setWorkflowName(dag.getWorkflowName());
            definition.setWorkflowDescription(dag.getDescription());
            definition.setDagJson(objectMapper.writeValueAsString(dag));
            definition.setClusterId(clusterId);
            definition.setVersion(1);
            definition.setStatus("ENABLED");
            definition.setCreatedBy(createdBy);

            save(definition);
            log.info("工作流创建成功: {}", definition.getWorkflowName());

            return definition;
        } catch (Exception e) {
            log.error("创建工作流失败", e);
            throw new RuntimeException("创建工作流失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition updateWorkflow(Long workflowId, WorkflowDAG dag) {
        WorkflowDefinition definition = getById(workflowId);
        if (definition == null) {
            throw new RuntimeException("工作流不存在: " + workflowId);
        }

        // 验证 DAG
        if (!dagValidator.validateDAG(dag)) {
            throw new RuntimeException("工作流 DAG 验证失败");
        }

        try {
            definition.setWorkflowName(dag.getWorkflowName());
            definition.setWorkflowDescription(dag.getDescription());
            definition.setDagJson(objectMapper.writeValueAsString(dag));
            definition.setVersion(definition.getVersion() + 1);

            updateById(definition);
            log.info("工作流更新成功: {}, 版本: {}", definition.getWorkflowName(), definition.getVersion());

            return definition;
        } catch (Exception e) {
            log.error("更新工作流失败", e);
            throw new RuntimeException("更新工作流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public WorkflowDAG parseWorkflowDAG(WorkflowDefinition definition) {
        try {
            WorkflowDAG dag = objectMapper.readValue(definition.getDagJson(), WorkflowDAG.class);
            dag.setWorkflowId(definition.getId());
            return dag;
        } catch (Exception e) {
            log.error("解析工作流 DAG 失败", e);
            throw new RuntimeException("解析工作流 DAG 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public WorkflowInstance executeWorkflow(Long workflowId) {
        WorkflowDefinition definition = getById(workflowId);
        if (definition == null) {
            throw new RuntimeException("工作流不存在: " + workflowId);
        }

        if (!"ENABLED".equals(definition.getStatus())) {
            throw new RuntimeException("工作流未启用: " + definition.getWorkflowName());
        }

        // 解析 DAG
        WorkflowDAG dag = parseWorkflowDAG(definition);

        // 执行工作流
        WorkflowInstance instance = dagExecutor.executeWorkflow(dag);

        // 记录执行日志
        saveExecutionLog(instance);

        return instance;
    }

    @Override
    public WorkflowInstance getWorkflowInstance(String instanceId) {
        return dagExecutor.getInstance(instanceId);
    }

    @Override
    public void cancelWorkflow(String instanceId) {
        dagExecutor.cancelWorkflow(instanceId);
    }

    @Override
    public boolean validateWorkflow(WorkflowDAG dag) {
        return dagValidator.validateDAG(dag);
    }

    /**
     * 保存执行日志
     */
    private void saveExecutionLog(WorkflowInstance instance) {
        try {
            WorkflowExecutionLog log = new WorkflowExecutionLog();
            log.setWorkflowId(instance.getWorkflowId());
            log.setInstanceId(instance.getInstanceId());
            log.setWorkflowName(instance.getWorkflowName());
            log.setStartTime(instance.getStartTime());
            log.setEndTime(instance.getEndTime());

            if (instance.getEndTime() != null) {
                log.setDurationMs(Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis());
            }

            log.setStatus(instance.getStatus());
            log.setExecutionParams(instance.getExecutionParams());
            log.setNodeStatuses(objectMapper.writeValueAsString(instance.getNodeStatuses()));
            log.setErrorMessage(instance.getErrorMessage());

            executionLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存工作流执行日志失败", e);
        }
    }
}
