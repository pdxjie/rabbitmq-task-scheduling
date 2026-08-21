package com.taskflow.workflow.engine;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.service.TaskDefinitionService;
import com.taskflow.service.TaskExecutorService;
import com.taskflow.workflow.model.WorkflowDAG;
import com.taskflow.workflow.model.WorkflowInstance;
import com.taskflow.workflow.model.WorkflowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * DAG 工作流执行引擎
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DAGExecutor {

    private final DAGValidator dagValidator;
    private final TaskDefinitionService taskService;
    private final TaskExecutorService taskExecutor;
    private final ObjectMapper objectMapper;

    /**
     * 线程池（用于并行执行）
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 正在执行的工作流实例
     */
    private final Map<String, WorkflowInstance> runningInstances = new ConcurrentHashMap<>();

    /**
     * 执行工作流
     */
    public WorkflowInstance executeWorkflow(WorkflowDAG dag) {
        // 验证 DAG
        if (!dagValidator.validateDAG(dag)) {
            throw new RuntimeException("工作流 DAG 验证失败");
        }

        // 创建工作流实例
        WorkflowInstance instance = createInstance(dag);
        runningInstances.put(instance.getInstanceId(), instance);

        log.info("开始执行工作流: {}, 实例ID: {}", dag.getWorkflowName(), instance.getInstanceId());

        // 异步执行工作流
        CompletableFuture.runAsync(() -> {
            try {
                executeDAG(dag, instance);
            } catch (Exception e) {
                log.error("工作流执行失败: {}", instance.getInstanceId(), e);
                instance.setStatus("FAILED");
                instance.setErrorMessage(e.getMessage());
                instance.setEndTime(LocalDateTime.now());
            } finally {
                runningInstances.remove(instance.getInstanceId());
            }
        }, executorService);

        return instance;
    }

    /**
     * 执行 DAG
     */
    private void executeDAG(WorkflowDAG dag, WorkflowInstance instance) {
        Set<String> completedNodes = ConcurrentHashMap.newKeySet();
        Set<String> failedNodes = ConcurrentHashMap.newKeySet();
        Set<String> runningNodes = ConcurrentHashMap.newKeySet();

        while (completedNodes.size() + failedNodes.size() < dag.getNodes().size()) {
            // 查找所有就绪的节点
            List<WorkflowNode> readyNodes = findReadyNodes(dag, completedNodes, failedNodes, runningNodes);

            if (readyNodes.isEmpty()) {
                // 没有就绪节点，检查是否有节点在运行
                if (runningNodes.isEmpty()) {
                    // 没有运行中的节点，说明工作流卡住了
                    log.warn("工作流卡住: {}", instance.getInstanceId());
                    break;
                }
                // 等待运行中的节点完成
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            // 并行执行所有就绪节点
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (WorkflowNode node : readyNodes) {
                runningNodes.add(node.getNodeId());

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        executeNode(node, instance);
                        completedNodes.add(node.getNodeId());
                        runningNodes.remove(node.getNodeId());
                        log.info("节点执行成功: {}", node.getNodeId());
                    } catch (Exception e) {
                        failedNodes.add(node.getNodeId());
                        runningNodes.remove(node.getNodeId());
                        log.error("节点执行失败: {}", node.getNodeId(), e);

                        // 如果不允许失败，标记整个工作流失败
                        if (!node.getAllowFailure()) {
                            instance.setStatus("FAILED");
                            instance.setErrorMessage("节点 " + node.getNodeId() + " 执行失败: " + e.getMessage());
                        }
                    }
                }, executorService);

                futures.add(future);
            }

            // 等待这一批节点完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // 判断工作流执行结果
        if (failedNodes.isEmpty()) {
            instance.setStatus("SUCCESS");
            log.info("工作流执行成功: {}", instance.getInstanceId());
        } else if (!"FAILED".equals(instance.getStatus())) {
            instance.setStatus("PARTIAL_SUCCESS");
            log.warn("工作流部分成功: {}, 失败节点: {}", instance.getInstanceId(), failedNodes);
        }

        instance.setEndTime(LocalDateTime.now());
    }

    /**
     * 查找就绪的节点
     */
    private List<WorkflowNode> findReadyNodes(WorkflowDAG dag, Set<String> completedNodes,
                                               Set<String> failedNodes, Set<String> runningNodes) {
        List<WorkflowNode> readyNodes = new ArrayList<>();

        for (WorkflowNode node : dag.getNodes()) {
            String nodeId = node.getNodeId();

            // 跳过已完成、已失败、正在运行的节点
            if (completedNodes.contains(nodeId) || failedNodes.contains(nodeId) || runningNodes.contains(nodeId)) {
                continue;
            }

            // 检查依赖是否都已完成
            if (dagValidator.isNodeReady(dag, nodeId, completedNodes)) {
                readyNodes.add(node);
            }
        }

        return readyNodes;
    }

    /**
     * 执行单个节点
     */
    private void executeNode(WorkflowNode node, WorkflowInstance instance) {
        WorkflowInstance.NodeExecutionStatus status = new WorkflowInstance.NodeExecutionStatus();
        status.setNodeId(node.getNodeId());
        status.setStatus("RUNNING");
        status.setStartTime(LocalDateTime.now());

        instance.getNodeStatuses().put(node.getNodeId(), status);

        try {
            log.info("开始执行节点: {}, 类型: {}", node.getNodeId(), node.getNodeType());

            switch (node.getNodeType()) {
                case "TASK" -> executeTaskNode(node, status);
                case "CONDITION" -> executeConditionNode(node, status);
                case "PARALLEL" -> executeParallelNode(node, status);
                case "AGGREGATE" -> executeAggregateNode(node, status);
                default -> throw new RuntimeException("不支持的节点类型: " + node.getNodeType());
            }

            status.setStatus("SUCCESS");
            status.setEndTime(LocalDateTime.now());

        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.setEndTime(LocalDateTime.now());
            throw e;
        }
    }

    /**
     * 执行任务节点
     */
    private void executeTaskNode(WorkflowNode node, WorkflowInstance.NodeExecutionStatus status) {
        if (node.getTaskId() == null) {
            throw new RuntimeException("任务节点未指定任务ID");
        }

        TaskDefinition task = taskService.getById(node.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在: " + node.getTaskId());
        }

        log.info("执行任务: {}", task.getTaskName());
        taskExecutor.executeTask(task);

        status.setResult("任务执行完成: " + task.getTaskName());
    }

    /**
     * 执行条件节点
     */
    private void executeConditionNode(WorkflowNode node, WorkflowInstance.NodeExecutionStatus status) {
        // TODO: 实现条件判断逻辑
        log.info("执行条件节点: {}", node.getCondition());
        status.setResult("条件判断完成");
    }

    /**
     * 执行并行节点
     */
    private void executeParallelNode(WorkflowNode node, WorkflowInstance.NodeExecutionStatus status) {
        // 并行节点本身不执行操作，由引擎自动并行执行后续节点
        log.info("进入并行分支: {}", node.getNodeId());
        status.setResult("并行分支");
    }

    /**
     * 执行聚合节点
     */
    private void executeAggregateNode(WorkflowNode node, WorkflowInstance.NodeExecutionStatus status) {
        // 聚合节点等待所有前驱节点完成
        log.info("等待聚合: {}", node.getNodeId());
        status.setResult("聚合完成");
    }

    /**
     * 创建工作流实例
     */
    private WorkflowInstance createInstance(WorkflowDAG dag) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setInstanceId(IdUtil.fastSimpleUUID());
        instance.setWorkflowId(dag.getWorkflowId());
        instance.setWorkflowName(dag.getWorkflowName());
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instance.setExecutionParams(dag.getGlobalParams());

        return instance;
    }

    /**
     * 获取工作流实例
     */
    public WorkflowInstance getInstance(String instanceId) {
        return runningInstances.get(instanceId);
    }

    /**
     * 取消工作流执行
     */
    public void cancelWorkflow(String instanceId) {
        WorkflowInstance instance = runningInstances.get(instanceId);
        if (instance != null) {
            instance.setStatus("CANCELLED");
            instance.setEndTime(LocalDateTime.now());
            runningInstances.remove(instanceId);
            log.info("工作流已取消: {}", instanceId);
        }
    }
}
