package com.taskflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.common.Result;
import com.taskflow.entity.WorkflowDefinition;
import com.taskflow.entity.WorkflowExecutionLog;
import com.taskflow.mapper.WorkflowExecutionLogMapper;
import com.taskflow.service.WorkflowService;
import com.taskflow.workflow.model.WorkflowDAG;
import com.taskflow.workflow.model.WorkflowInstance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流管理控制器
 */
@Tag(name = "工作流管理", description = "DAG 工作流编排和执行")
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowExecutionLogMapper executionLogMapper;

    @Operation(summary = "创建工作流")
    @PostMapping
    public Result<WorkflowDefinition> createWorkflow(
            @Validated @RequestBody WorkflowDAG dag,
            @RequestParam Long clusterId,
            @RequestParam(required = false) String createdBy) {

        WorkflowDefinition workflow = workflowService.createWorkflow(dag, clusterId, createdBy);
        return Result.success("工作流创建成功", workflow);
    }

    @Operation(summary = "更新工作流")
    @PutMapping("/{id}")
    public Result<WorkflowDefinition> updateWorkflow(
            @PathVariable Long id,
            @RequestBody WorkflowDAG dag) {

        WorkflowDefinition workflow = workflowService.updateWorkflow(id, dag);
        return Result.success("工作流更新成功", workflow);
    }

    @Operation(summary = "获取工作流详情")
    @GetMapping("/{id}")
    public Result<WorkflowDefinition> getWorkflow(@PathVariable Long id) {
        WorkflowDefinition workflow = workflowService.getById(id);
        return workflow != null ? Result.success(workflow) : Result.error("工作流不存在");
    }

    @Operation(summary = "获取工作流列表")
    @GetMapping("/list")
    public Result<List<WorkflowDefinition>> listWorkflows(
            @RequestParam(required = false) Long clusterId,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        if (clusterId != null) {
            wrapper.eq(WorkflowDefinition::getClusterId, clusterId);
        }
        if (status != null) {
            wrapper.eq(WorkflowDefinition::getStatus, status);
        }
        wrapper.orderByDesc(WorkflowDefinition::getCreatedAt);

        return Result.success(workflowService.list(wrapper));
    }

    @Operation(summary = "分页查询工作流")
    @GetMapping("/page")
    public Result<Page<WorkflowDefinition>> pageWorkflows(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<WorkflowDefinition> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<WorkflowDefinition>()
                .orderByDesc(WorkflowDefinition::getCreatedAt);

        return Result.success(workflowService.page(page, wrapper));
    }

    @Operation(summary = "删除工作流")
    @DeleteMapping("/{id}")
    public Result<String> deleteWorkflow(@PathVariable Long id) {
        boolean success = workflowService.removeById(id);
        return success ? Result.success("工作流删除成功") : Result.error("工作流删除失败");
    }

    @Operation(summary = "验证工作流 DAG")
    @PostMapping("/validate")
    public Result<String> validateWorkflow(@RequestBody WorkflowDAG dag) {
        boolean valid = workflowService.validateWorkflow(dag);
        return valid ? Result.success("工作流 DAG 验证通过") : Result.error("工作流 DAG 验证失败");
    }

    @Operation(summary = "执行工作流")
    @PostMapping("/{id}/execute")
    public Result<WorkflowInstance> executeWorkflow(@PathVariable Long id) {
        try {
            WorkflowInstance instance = workflowService.executeWorkflow(id);
            return Result.success("工作流已提交执行", instance);
        } catch (Exception e) {
            return Result.error("工作流执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取工作流执行实例")
    @GetMapping("/instance/{instanceId}")
    public Result<WorkflowInstance> getWorkflowInstance(@PathVariable String instanceId) {
        WorkflowInstance instance = workflowService.getWorkflowInstance(instanceId);
        return instance != null ? Result.success(instance) : Result.error("工作流实例不存在");
    }

    @Operation(summary = "取消工作流执行")
    @PostMapping("/instance/{instanceId}/cancel")
    public Result<String> cancelWorkflow(@PathVariable String instanceId) {
        workflowService.cancelWorkflow(instanceId);
        return Result.success("工作流已取消");
    }

    @Operation(summary = "获取工作流执行历史")
    @GetMapping("/{id}/history")
    public Result<Page<WorkflowExecutionLog>> getWorkflowHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<WorkflowExecutionLog> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkflowExecutionLog> wrapper = new LambdaQueryWrapper<WorkflowExecutionLog>()
                .eq(WorkflowExecutionLog::getWorkflowId, id)
                .orderByDesc(WorkflowExecutionLog::getStartTime);

        return Result.success(executionLogMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "解析工作流 DAG")
    @GetMapping("/{id}/dag")
    public Result<WorkflowDAG> getWorkflowDAG(@PathVariable Long id) {
        WorkflowDefinition definition = workflowService.getById(id);
        if (definition == null) {
            return Result.error("工作流不存在");
        }

        WorkflowDAG dag = workflowService.parseWorkflowDAG(definition);
        return Result.success(dag);
    }
}
