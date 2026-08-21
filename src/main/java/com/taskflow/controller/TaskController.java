package com.taskflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.common.Result;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.entity.TaskExecutionLog;
import com.taskflow.mapper.TaskExecutionLogMapper;
import com.taskflow.service.TaskDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理", description = "任务定义和执行管理接口")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskDefinitionService taskService;
    private final TaskExecutionLogMapper logMapper;

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<TaskDefinition> createTask(@Validated @RequestBody CreateTaskRequest request) {
        // 参数校验
        if ("CRON".equals(request.getTriggerType()) && request.getCronExpression() == null) {
            return Result.error("Cron 表达式不能为空");
        }
        if ("DELAY".equals(request.getTriggerType()) && request.getDelaySeconds() == null) {
            return Result.error("延迟秒数不能为空");
        }

        TaskDefinition task = new TaskDefinition();
        BeanUtils.copyProperties(request, task);

        TaskDefinition created = taskService.createTask(task);
        return Result.success("任务创建成功", created);
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<String> updateTask(@PathVariable Long id, @RequestBody CreateTaskRequest request) {
        TaskDefinition task = new TaskDefinition();
        BeanUtils.copyProperties(request, task);
        task.setId(id);

        boolean success = taskService.updateTask(task);
        return success ? Result.success("任务更新成功") : Result.error("任务更新失败");
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskDefinition> getTask(@PathVariable Long id) {
        TaskDefinition task = taskService.getById(id);
        return task != null ? Result.success(task) : Result.error("任务不存在");
    }

    @Operation(summary = "获取任务列表")
    @GetMapping("/list")
    public Result<List<TaskDefinition>> listTasks(
            @RequestParam(required = false) Long clusterId,
            @RequestParam(required = false) String status) {

        LambdaQueryWrapper<TaskDefinition> wrapper = new LambdaQueryWrapper<>();
        if (clusterId != null) {
            wrapper.eq(TaskDefinition::getClusterId, clusterId);
        }
        if (status != null) {
            wrapper.eq(TaskDefinition::getStatus, status);
        }
        wrapper.orderByDesc(TaskDefinition::getCreatedAt);

        return Result.success(taskService.list(wrapper));
    }

    @Operation(summary = "分页查询任务")
    @GetMapping("/page")
    public Result<Page<TaskDefinition>> pageTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long clusterId) {

        Page<TaskDefinition> page = new Page<>(current, size);
        LambdaQueryWrapper<TaskDefinition> wrapper = new LambdaQueryWrapper<>();
        if (clusterId != null) {
            wrapper.eq(TaskDefinition::getClusterId, clusterId);
        }
        wrapper.orderByDesc(TaskDefinition::getCreatedAt);

        return Result.success(taskService.page(page, wrapper));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<String> deleteTask(@PathVariable Long id) {
        boolean success = taskService.removeById(id);
        return success ? Result.success("任务删除成功") : Result.error("任务删除失败");
    }

    @Operation(summary = "启用任务")
    @PostMapping("/{id}/enable")
    public Result<String> enableTask(@PathVariable Long id) {
        boolean success = taskService.enableTask(id);
        return success ? Result.success("任务已启用") : Result.error("任务启用失败");
    }

    @Operation(summary = "禁用任务")
    @PostMapping("/{id}/disable")
    public Result<String> disableTask(@PathVariable Long id) {
        boolean success = taskService.disableTask(id);
        return success ? Result.success("任务已禁用") : Result.error("任务禁用失败");
    }

    @Operation(summary = "立即执行任务")
    @PostMapping("/{id}/execute")
    public Result<String> executeTask(@PathVariable Long id) {
        try {
            taskService.executeTaskNow(id);
            return Result.success("任务已提交执行");
        } catch (Exception e) {
            return Result.error("任务执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取任务执行日志")
    @GetMapping("/{id}/logs")
    public Result<Page<TaskExecutionLog>> getTaskLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<TaskExecutionLog> page = new Page<>(current, size);
        LambdaQueryWrapper<TaskExecutionLog> wrapper = new LambdaQueryWrapper<TaskExecutionLog>()
                .eq(TaskExecutionLog::getTaskId, id)
                .orderByDesc(TaskExecutionLog::getStartTime);

        return Result.success(logMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "根据 TraceId 查询日志")
    @GetMapping("/trace/{traceId}")
    public Result<TaskExecutionLog> getLogByTraceId(@PathVariable String traceId) {
        TaskExecutionLog log = logMapper.selectOne(
                new LambdaQueryWrapper<TaskExecutionLog>()
                        .eq(TaskExecutionLog::getTraceId, traceId)
        );
        return log != null ? Result.success(log) : Result.error("日志不存在");
    }
}
