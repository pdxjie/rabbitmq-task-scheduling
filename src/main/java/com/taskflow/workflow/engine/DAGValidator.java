package com.taskflow.workflow.engine;

import com.taskflow.workflow.model.WorkflowDAG;
import com.taskflow.workflow.model.WorkflowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DAG 拓扑排序和验证
 */
@Slf4j
@Component
public class DAGValidator {

    /**
     * 验证 DAG 合法性（无环）
     */
    public boolean validateDAG(WorkflowDAG dag) {
        try {
            // 尝试拓扑排序，如果有环会抛出异常
            topologicalSort(dag);
            return true;
        } catch (Exception e) {
            log.error("DAG 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 拓扑排序
     * 使用 Kahn 算法
     */
    public List<String> topologicalSort(WorkflowDAG dag) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacencyList = new HashMap<>();

        // 初始化入度和邻接表
        for (WorkflowNode node : dag.getNodes()) {
            inDegree.put(node.getNodeId(), 0);
            adjacencyList.put(node.getNodeId(), new ArrayList<>());
        }

        // 构建图
        for (WorkflowDAG.WorkflowEdge edge : dag.getEdges()) {
            adjacencyList.get(edge.getSource()).add(edge.getTarget());
            inDegree.put(edge.getTarget(), inDegree.get(edge.getTarget()) + 1);
        }

        // 找出所有入度为 0 的节点
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);

            // 遍历所有相邻节点
            for (String neighbor : adjacencyList.get(node)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 如果拓扑排序的结果数量不等于节点数量，说明有环
        if (result.size() != dag.getNodes().size()) {
            throw new RuntimeException("工作流存在循环依赖");
        }

        return result;
    }

    /**
     * 获取根节点（没有依赖的节点）
     */
    public List<WorkflowNode> getRootNodes(WorkflowDAG dag) {
        Set<String> hasIncomingEdge = dag.getEdges().stream()
                .map(WorkflowDAG.WorkflowEdge::getTarget)
                .collect(Collectors.toSet());

        return dag.getNodes().stream()
                .filter(node -> !hasIncomingEdge.contains(node.getNodeId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取节点的直接后继节点
     */
    public List<WorkflowNode> getSuccessors(WorkflowDAG dag, String nodeId) {
        List<String> successorIds = dag.getEdges().stream()
                .filter(edge -> edge.getSource().equals(nodeId))
                .map(WorkflowDAG.WorkflowEdge::getTarget)
                .collect(Collectors.toList());

        return dag.getNodes().stream()
                .filter(node -> successorIds.contains(node.getNodeId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取节点的所有前驱节点
     */
    public List<WorkflowNode> getPredecessors(WorkflowDAG dag, String nodeId) {
        List<String> predecessorIds = dag.getEdges().stream()
                .filter(edge -> edge.getTarget().equals(nodeId))
                .map(WorkflowDAG.WorkflowEdge::getSource)
                .collect(Collectors.toList());

        return dag.getNodes().stream()
                .filter(node -> predecessorIds.contains(node.getNodeId()))
                .collect(Collectors.toList());
    }

    /**
     * 检查节点是否就绪（所有依赖都已完成）
     */
    public boolean isNodeReady(WorkflowDAG dag, String nodeId, Set<String> completedNodes) {
        List<WorkflowNode> predecessors = getPredecessors(dag, nodeId);
        for (WorkflowNode predecessor : predecessors) {
            if (!completedNodes.contains(predecessor.getNodeId())) {
                return false;
            }
        }
        return true;
    }
}
