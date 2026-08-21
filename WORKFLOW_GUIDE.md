# DAG 工作流使用指南

## 一、工作流概念

### 1.1 什么是 DAG 工作流

DAG（Directed Acyclic Graph，有向无环图）工作流是一种任务编排方式，支持：
- ✅ **任务依赖**：定义任务之间的执行顺序
- ✅ **并行执行**：多个独立任务同时执行
- ✅ **条件分支**：根据条件选择执行路径
- ✅ **失败处理**：失败重试、容错、回滚

### 1.2 节点类型

| 类型 | 说明 | 使用场景 |
|------|------|----------|
| **TASK** | 执行具体任务 | 发送消息、调用 API、执行脚本 |
| **CONDITION** | 条件判断 | 根据前置任务结果选择分支 |
| **PARALLEL** | 并行分支 | 多个任务同时执行 |
| **AGGREGATE** | 聚合节点 | 等待多个并行任务完成 |

---

## 二、创建工作流

### 2.1 简单串行工作流

```json
{
  "workflowName": "订单处理流程",
  "description": "订单创建 → 支付 → 发货",
  "nodes": [
    {
      "nodeId": "node_1",
      "nodeName": "创建订单",
      "nodeType": "TASK",
      "taskId": 1,
      "dependencies": []
    },
    {
      "nodeId": "node_2",
      "nodeName": "处理支付",
      "nodeType": "TASK",
      "taskId": 2,
      "dependencies": ["node_1"]
    },
    {
      "nodeId": "node_3",
      "nodeName": "安排发货",
      "nodeType": "TASK",
      "taskId": 3,
      "dependencies": ["node_2"]
    }
  ],
  "edges": [
    { "source": "node_1", "target": "node_2", "condition": "SUCCESS" },
    { "source": "node_2", "target": "node_3", "condition": "SUCCESS" }
  ],
  "timeoutSeconds": 600
}
```

**执行流程**：
```
node_1 (创建订单) → node_2 (处理支付) → node_3 (安排发货)
```

### 2.2 并行工作流

```json
{
  "workflowName": "数据同步流程",
  "description": "并行同步多个数据源",
  "nodes": [
    {
      "nodeId": "start",
      "nodeName": "开始",
      "nodeType": "PARALLEL",
      "dependencies": []
    },
    {
      "nodeId": "sync_user",
      "nodeName": "同步用户数据",
      "nodeType": "TASK",
      "taskId": 10,
      "dependencies": ["start"]
    },
    {
      "nodeId": "sync_order",
      "nodeName": "同步订单数据",
      "nodeType": "TASK",
      "taskId": 11,
      "dependencies": ["start"]
    },
    {
      "nodeId": "sync_product",
      "nodeName": "同步商品数据",
      "nodeType": "TASK",
      "taskId": 12,
      "dependencies": ["start"]
    },
    {
      "nodeId": "aggregate",
      "nodeName": "汇总结果",
      "nodeType": "AGGREGATE",
      "dependencies": ["sync_user", "sync_order", "sync_product"]
    }
  ],
  "edges": [
    { "source": "start", "target": "sync_user" },
    { "source": "start", "target": "sync_order" },
    { "source": "start", "target": "sync_product" },
    { "source": "sync_user", "target": "aggregate" },
    { "source": "sync_order", "target": "aggregate" },
    { "source": "sync_product", "target": "aggregate" }
  ]
}
```

**执行流程**：
```
              ┌─→ sync_user ─┐
start (并行) ─┼─→ sync_order ─┼─→ aggregate (聚合)
              └─→ sync_product─┘
```

### 2.3 条件分支工作流

```json
{
  "workflowName": "智能审批流程",
  "description": "根据金额自动审批或人工审批",
  "nodes": [
    {
      "nodeId": "check_amount",
      "nodeName": "检查金额",
      "nodeType": "TASK",
      "taskId": 20,
      "dependencies": []
    },
    {
      "nodeId": "condition_1",
      "nodeName": "判断金额",
      "nodeType": "CONDITION",
      "condition": "amount < 1000",
      "dependencies": ["check_amount"]
    },
    {
      "nodeId": "auto_approve",
      "nodeName": "自动通过",
      "nodeType": "TASK",
      "taskId": 21,
      "dependencies": ["condition_1"]
    },
    {
      "nodeId": "manual_approve",
      "nodeName": "人工审批",
      "nodeType": "TASK",
      "taskId": 22,
      "dependencies": ["condition_1"]
    }
  ],
  "edges": [
    { "source": "check_amount", "target": "condition_1" },
    { "source": "condition_1", "target": "auto_approve", "condition": "SUCCESS" },
    { "source": "condition_1", "target": "manual_approve", "condition": "FAILED" }
  ]
}
```

---

## 三、API 使用示例

### 3.1 创建工作流

```bash
curl -X POST "http://localhost:8080/api/workflow?clusterId=1&createdBy=admin" \
  -H "Content-Type: application/json" \
  -d '{
    "workflowName": "测试工作流",
    "description": "简单的测试流程",
    "nodes": [
      {
        "nodeId": "node_1",
        "nodeName": "任务1",
        "nodeType": "TASK",
        "taskId": 1,
        "dependencies": []
      },
      {
        "nodeId": "node_2",
        "nodeName": "任务2",
        "nodeType": "TASK",
        "taskId": 2,
        "dependencies": ["node_1"]
      }
    ],
    "edges": [
      { "source": "node_1", "target": "node_2", "condition": "SUCCESS" }
    ]
  }'
```

### 3.2 验证工作流

```bash
curl -X POST http://localhost:8080/api/workflow/validate \
  -H "Content-Type: application/json" \
  -d '{
    "nodes": [...],
    "edges": [...]
  }'
```

### 3.3 执行工作流

```bash
# 执行工作流（假设工作流 ID 为 1）
curl -X POST http://localhost:8080/api/workflow/1/execute
```

返回：
```json
{
  "code": 200,
  "message": "工作流已提交执行",
  "data": {
    "instanceId": "abc123xyz",
    "workflowId": 1,
    "workflowName": "测试工作流",
    "status": "RUNNING",
    "startTime": "2026-08-21T15:30:00",
    "nodeStatuses": {
      "node_1": {
        "status": "RUNNING",
        "startTime": "2026-08-21T15:30:00"
      }
    }
  }
}
```

### 3.4 查询工作流执行状态

```bash
curl http://localhost:8080/api/workflow/instance/abc123xyz
```

返回：
```json
{
  "code": 200,
  "data": {
    "instanceId": "abc123xyz",
    "status": "SUCCESS",
    "startTime": "2026-08-21T15:30:00",
    "endTime": "2026-08-21T15:32:00",
    "nodeStatuses": {
      "node_1": {
        "status": "SUCCESS",
        "startTime": "2026-08-21T15:30:00",
        "endTime": "2026-08-21T15:31:00",
        "result": "任务执行完成: 任务1"
      },
      "node_2": {
        "status": "SUCCESS",
        "startTime": "2026-08-21T15:31:00",
        "endTime": "2026-08-21T15:32:00",
        "result": "任务执行完成: 任务2"
      }
    }
  }
}
```

### 3.5 取消工作流执行

```bash
curl -X POST http://localhost:8080/api/workflow/instance/abc123xyz/cancel
```

### 3.6 查询工作流执行历史

```bash
curl "http://localhost:8080/api/workflow/1/history?current=1&size=20"
```

---

## 四、工作流示例

### 示例1：ETL 数据处理流程

```json
{
  "workflowName": "ETL 数据处理",
  "description": "提取 → 转换 → 加载",
  "nodes": [
    {
      "nodeId": "extract",
      "nodeName": "提取数据",
      "nodeType": "TASK",
      "taskId": 100,
      "timeoutSeconds": 300
    },
    {
      "nodeId": "transform",
      "nodeName": "数据转换",
      "nodeType": "TASK",
      "taskId": 101,
      "dependencies": ["extract"]
    },
    {
      "nodeId": "load",
      "nodeName": "加载数据",
      "nodeType": "TASK",
      "taskId": 102,
      "dependencies": ["transform"]
    },
    {
      "nodeId": "notify",
      "nodeName": "发送通知",
      "nodeType": "TASK",
      "taskId": 103,
      "dependencies": ["load"],
      "allowFailure": true
    }
  ],
  "edges": [
    { "source": "extract", "target": "transform" },
    { "source": "transform", "target": "load" },
    { "source": "load", "target": "notify" }
  ]
}
```

### 示例2：微服务部署流程

```json
{
  "workflowName": "微服务部署",
  "description": "构建 → 测试 → 部署",
  "nodes": [
    {
      "nodeId": "build",
      "nodeName": "构建镜像",
      "nodeType": "TASK",
      "taskId": 200
    },
    {
      "nodeId": "test_parallel",
      "nodeName": "并行测试",
      "nodeType": "PARALLEL",
      "dependencies": ["build"]
    },
    {
      "nodeId": "unit_test",
      "nodeName": "单元测试",
      "nodeType": "TASK",
      "taskId": 201,
      "dependencies": ["test_parallel"]
    },
    {
      "nodeId": "integration_test",
      "nodeName": "集成测试",
      "nodeType": "TASK",
      "taskId": 202,
      "dependencies": ["test_parallel"]
    },
    {
      "nodeId": "aggregate_test",
      "nodeName": "测试聚合",
      "nodeType": "AGGREGATE",
      "dependencies": ["unit_test", "integration_test"]
    },
    {
      "nodeId": "deploy_staging",
      "nodeName": "部署到预发布",
      "nodeType": "TASK",
      "taskId": 203,
      "dependencies": ["aggregate_test"]
    },
    {
      "nodeId": "smoke_test",
      "nodeName": "冒烟测试",
      "nodeType": "TASK",
      "taskId": 204,
      "dependencies": ["deploy_staging"]
    },
    {
      "nodeId": "deploy_production",
      "nodeName": "部署到生产",
      "nodeType": "TASK",
      "taskId": 205,
      "dependencies": ["smoke_test"]
    }
  ],
  "edges": [
    { "source": "build", "target": "test_parallel" },
    { "source": "test_parallel", "target": "unit_test" },
    { "source": "test_parallel", "target": "integration_test" },
    { "source": "unit_test", "target": "aggregate_test" },
    { "source": "integration_test", "target": "aggregate_test" },
    { "source": "aggregate_test", "target": "deploy_staging" },
    { "source": "deploy_staging", "target": "smoke_test" },
    { "source": "smoke_test", "target": "deploy_production" }
  ]
}
```

---

## 五、执行流程

### 5.1 工作流执行过程

```
1. 验证 DAG（检测循环依赖）
    ↓
2. 创建工作流实例
    ↓
3. 拓扑排序（确定执行顺序）
    ↓
4. 查找根节点（无依赖节点）
    ↓
5. 并行执行就绪节点
    ↓
6. 等待节点完成
    ↓
7. 查找新的就绪节点
    ↓
8. 重复步骤 5-7，直到所有节点完成
    ↓
9. 记录执行日志
```

### 5.2 节点状态转换

```
PENDING（等待） → RUNNING（执行中） → SUCCESS（成功）
                                    ↓
                                  FAILED（失败）
                                    ↓
                                  SKIPPED（跳过）
```

---

## 六、最佳实践

### 6.1 节点命名规范

- ✅ 使用有意义的名称：`extract_user_data`
- ✅ 使用下划线分隔：`check_order_status`
- ❌ 避免使用数字编号：`node_1`, `task_2`

### 6.2 错误处理

```json
{
  "nodeId": "send_notification",
  "nodeName": "发送通知",
  "nodeType": "TASK",
  "taskId": 10,
  "allowFailure": true,  // 允许失败，不影响整个工作流
  "timeoutSeconds": 30   // 设置超时时间
}
```

### 6.3 性能优化

1. **合理使用并行**：独立任务并行执行
2. **设置超时时间**：防止任务无限等待
3. **减少依赖层级**：避免过深的依赖链
4. **控制节点数量**：单个工作流不超过 100 个节点

### 6.4 避免环形依赖

❌ **错误示例**：
```
node_1 → node_2 → node_3 → node_1  (形成环)
```

✅ **正确示例**：
```
node_1 → node_2 → node_3  (线性依赖)
```

---

## 七、故障排查

### 7.1 工作流无法创建

**问题**：创建工作流时报错 "工作流 DAG 验证失败"

**解决**：
1. 检查是否存在循环依赖
2. 确保所有节点 ID 唯一
3. 确保边的 source 和 target 对应存在的节点
4. 使用验证接口检查 DAG

### 7.2 工作流卡住不执行

**问题**：工作流状态一直是 RUNNING

**可能原因**：
1. 某个节点执行时间过长
2. 某个节点任务不存在
3. 依赖关系配置错误

**解决**：
1. 查看工作流实例状态，检查哪个节点卡住
2. 检查该节点对应的任务是否正常
3. 设置合理的超时时间

### 7.3 节点执行失败

**问题**：某个节点状态为 FAILED

**解决**：
1. 查看节点执行状态的错误信息
2. 检查对应任务的执行日志
3. 如果允许失败，设置 `allowFailure: true`

---

## 八、与任务调度的区别

| 特性 | 任务调度 | 工作流编排 |
|------|---------|-----------|
| 触发方式 | Cron/延迟/立即 | 手动触发 |
| 任务关系 | 独立执行 | 有依赖关系 |
| 并行能力 | 不支持 | 支持并行 |
| 条件分支 | 不支持 | 支持条件 |
| 适用场景 | 定时任务、单一任务 | 复杂流程、多步骤 |

---

完整 API 文档请访问：http://localhost:8080/api/swagger-ui.html
