# 监控和告警使用指南

## 一、监控功能

### 1.1 系统监控指标

**获取系统监控数据**：
```bash
curl http://localhost:8080/api/monitor/system
```

返回数据包括：
- **任务统计**：总数、启用数、运行中数量
- **执行统计**：今日执行次数、成功/失败数、成功率
- **平均执行时长**：任务平均耗时
- **工作流统计**：总数、运行中数量
- **集群统计**：总数、活跃集群数
- **JVM 指标**：堆内存使用、线程数
- **CPU 使用率**

示例响应：
```json
{
  "code": 200,
  "data": {
    "totalTasks": 25,
    "enabledTasks": 20,
    "runningTasks": 3,
    "todayExecutions": 1456,
    "todaySuccess": 1420,
    "todayFailed": 36,
    "successRate": 97.53,
    "avgExecutionTime": 2345.67,
    "totalWorkflows": 5,
    "runningWorkflows": 1,
    "totalClusters": 3,
    "activeClusters": 3,
    "jvmHeapUsed": 512,
    "jvmHeapMax": 2048,
    "threadCount": 45,
    "cpuUsage": 12.5,
    "collectTime": "2026-08-21T16:30:00"
  }
}
```

---

### 1.2 集群监控

**获取所有集群监控数据**：
```bash
curl http://localhost:8080/api/monitor/cluster/all
```

**获取指定集群监控数据**：
```bash
curl http://localhost:8080/api/monitor/cluster/1
```

返回数据包括：
- 节点数量、队列总数、交换机总数
- 连接数、通道数、消费者数量
- 消息总数、就绪消息数、未确认消息数
- 发布速率、消费速率、确认速率
- 内存使用、磁盘使用、文件描述符
- 运行时间、健康状态

---

### 1.3 队列监控

**获取集群所有队列监控数据**：
```bash
curl http://localhost:8080/api/monitor/cluster/1/queues
```

**获取指定队列监控数据**：
```bash
curl http://localhost:8080/api/monitor/cluster/1/queue/test.queue
```

返回数据包括：
- 队列名称、虚拟主机
- 消息总数、就绪消息数、未确认消息数
- 消费者数量、内存使用
- 发布速率、消费速率、确认速率
- 队列状态、是否持久化

---

### 1.4 监控数据刷新

**刷新指定集群监控数据**：
```bash
curl -X POST http://localhost:8080/api/monitor/cluster/1/refresh
```

**刷新所有集群监控数据**：
```bash
curl -X POST http://localhost:8080/api/monitor/refresh
```

---

## 二、告警功能

### 2.1 告警规则类型

| 规则类型 | 说明 | 触发条件 |
|---------|------|---------|
| **TASK_FAIL** | 任务失败告警 | 任务执行失败时触发 |
| **QUEUE_BACKLOG** | 队列积压告警 | 队列消息数超过阈值 |
| **CONSUMER_OFFLINE** | 消费者离线告警 | 队列无活跃消费者 |
| **EXECUTION_TIME** | 执行超时告警 | 任务执行时长超过阈值 |

### 2.2 创建告警规则

#### 示例1：任务失败告警

```bash
curl -X POST http://localhost:8080/api/alert/rule \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "任务失败告警",
    "ruleType": "TASK_FAIL",
    "conditionJson": "{\"type\":\"TASK_FAIL\"}",
    "notificationChannels": "DINGTALK,EMAIL",
    "notificationUsers": "admin@company.com",
    "status": "ENABLED"
  }'
```

#### 示例2：队列积压告警

```bash
curl -X POST http://localhost:8080/api/alert/rule \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "队列积压告警",
    "ruleType": "QUEUE_BACKLOG",
    "clusterId": 1,
    "conditionJson": "{\"type\":\"QUEUE_BACKLOG\",\"threshold\":1000,\"operator\":\"GT\"}",
    "notificationChannels": "DINGTALK,WECHAT",
    "status": "ENABLED"
  }'
```

**条件说明**：
- `threshold`: 阈值（如消息数、执行时长等）
- `operator`: 比较操作符
  - `GT`: 大于 (>)
  - `LT`: 小于 (<)
  - `EQ`: 等于 (=)
  - `GTE`: 大于等于 (>=)
  - `LTE`: 小于等于 (<=)

#### 示例3：执行超时告警

```bash
curl -X POST http://localhost:8080/api/alert/rule \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "任务执行超时告警",
    "ruleType": "EXECUTION_TIME",
    "conditionJson": "{\"type\":\"EXECUTION_TIME\",\"threshold\":300,\"operator\":\"GT\"}",
    "notificationChannels": "EMAIL,SMS",
    "notificationUsers": "admin@company.com,13800138000",
    "status": "ENABLED"
  }'
```

---

### 2.3 通知渠道

支持的通知渠道：
- **EMAIL**：邮件通知
- **DINGTALK**：钉钉机器人通知
- **WECHAT**：企业微信机器人通知
- **SMS**：短信通知

多个渠道用逗号分隔，如：`DINGTALK,EMAIL,SMS`

#### 钉钉通知配置

钉钉通知使用 Markdown 格式，消息示例：
```markdown
【任务失败】订单处理任务

**任务ID**: 123
**任务名称**: 订单处理
**失败时间**: 2026-08-21 15:30:00
**错误信息**: 连接数据库超时
```

#### 企业微信通知配置

企业微信通知也使用 Markdown 格式，消息格式与钉钉类似。

---

### 2.4 告警规则管理

**获取告警规则列表**：
```bash
curl "http://localhost:8080/api/alert/rule/list?ruleType=TASK_FAIL&clusterId=1"
```

**更新告警规则**：
```bash
curl -X PUT http://localhost:8080/api/alert/rule/1 \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "更新后的规则名称",
    "notificationChannels": "DINGTALK,EMAIL,SMS"
  }'
```

**启用/禁用告警规则**：
```bash
# 启用
curl -X POST http://localhost:8080/api/alert/rule/1/enable

# 禁用
curl -X POST http://localhost:8080/api/alert/rule/1/disable
```

**删除告警规则**：
```bash
curl -X DELETE http://localhost:8080/api/alert/rule/1
```

**测试告警发送**：
```bash
curl -X POST "http://localhost:8080/api/alert/test?ruleId=1"
```

---

### 2.5 查看告警记录

**分页查询告警记录**：
```bash
curl "http://localhost:8080/api/alert/record/page?current=1&size=20&level=ERROR&status=SENT"
```

**告警级别**：
- `INFO`: 信息
- `WARNING`: 警告
- `ERROR`: 错误
- `CRITICAL`: 严重

**告警状态**：
- `PENDING`: 待发送
- `SENT`: 已发送
- `FAILED`: 发送失败

---

## 三、WebSocket 实时推送

### 3.1 连接 WebSocket

**WebSocket 地址**：
```
ws://localhost:8080/ws/monitor/{clientId}
```

其中 `{clientId}` 是客户端唯一标识（可以是 UUID、用户ID 等）。

### 3.2 前端连接示例

**原生 JavaScript**：
```javascript
// 连接 WebSocket
const clientId = 'client_' + Date.now();
const ws = new WebSocket(`ws://localhost:8080/ws/monitor/${clientId}`);

// 连接成功
ws.onopen = function(event) {
    console.log('WebSocket 连接成功');
};

// 接收消息
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
    
    // 根据消息类型处理
    switch(message.type) {
        case 'system_metrics':
            updateSystemMetrics(message.data);
            break;
        case 'alert':
            showAlert(message.data);
            break;
        case 'task_status':
            updateTaskStatus(message.data);
            break;
    }
};

// 连接关闭
ws.onclose = function(event) {
    console.log('WebSocket 连接关闭');
};

// 发生错误
ws.onerror = function(error) {
    console.error('WebSocket 错误:', error);
};

// 发送消息（订阅特定数据）
ws.send('subscribe:cluster_1');
```

**React 示例**：
```jsx
import { useEffect, useState } from 'react';

function MonitorDashboard() {
    const [metrics, setMetrics] = useState(null);
    const [alerts, setAlerts] = useState([]);

    useEffect(() => {
        const clientId = 'client_' + Date.now();
        const ws = new WebSocket(`ws://localhost:8080/ws/monitor/${clientId}`);

        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            
            if (message.type === 'system_metrics') {
                setMetrics(message.data);
            } else if (message.type === 'alert') {
                setAlerts(prev => [message.data, ...prev]);
            }
        };

        return () => ws.close();
    }, []);

    return (
        <div>
            {metrics && (
                <div>
                    <h2>系统监控</h2>
                    <p>任务总数: {metrics.totalTasks}</p>
                    <p>今日执行: {metrics.todayExecutions}</p>
                    <p>成功率: {metrics.successRate}%</p>
                </div>
            )}
            
            <div>
                <h2>实时告警</h2>
                {alerts.map((alert, index) => (
                    <div key={index} className={`alert alert-${alert.level}`}>
                        <h4>{alert.title}</h4>
                        <p>{alert.content}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}
```

---

### 3.3 消息类型

#### 1. 系统监控数据（每 5 秒推送一次）

```json
{
  "type": "system_metrics",
  "data": {
    "totalTasks": 25,
    "enabledTasks": 20,
    "todayExecutions": 1456,
    "todaySuccess": 1420,
    "successRate": 97.53,
    ...
  },
  "timestamp": 1629532800000
}
```

#### 2. 告警消息

```json
{
  "type": "alert",
  "data": {
    "title": "【任务失败】订单处理任务",
    "content": "**任务ID**: 123\n**失败时间**: 2026-08-21 15:30:00",
    "level": "ERROR"
  },
  "timestamp": 1629532800000
}
```

#### 3. 任务状态更新

```json
{
  "type": "task_status",
  "data": {
    "taskId": 123,
    "taskName": "订单处理任务",
    "status": "SUCCESS"
  },
  "timestamp": 1629532800000
}
```

---

## 四、监控大屏设计建议

### 4.1 布局结构

```
┌─────────────────────────────────────────────────────────┐
│                   TaskFlow 监控大屏                      │
├────────────────┬────────────────┬───────────────────────┤
│                │                │                       │
│  任务总数      │  今日执行      │  成功率               │
│  1,234         │  12,456        │  98.5%                │
│                │                │                       │
├────────────────┴────────────────┴───────────────────────┤
│                                                          │
│  📈 实时吞吐量（消息/秒）                                │
│  [实时折线图]                                            │
│                                                          │
├──────────────────────────┬───────────────────────────────┤
│                          │                              │
│  🔥 热点任务 Top 5        │  ⚠️  实时告警                │
│  1. 订单处理 (1.2K)      │  🔴 队列积压: order.queue   │
│  2. 数据同步 (850)       │  🟡 执行超时: sync_task     │
│  3. 消息推送 (620)       │  🟢 系统正常                 │
│                          │                              │
└──────────────────────────┴───────────────────────────────┘
```

### 4.2 推荐图表库

- **ECharts**：功能强大，图表类型丰富
- **Recharts**：React 生态，易于集成
- **D3.js**：自定义程度高，适合复杂可视化

---

## 五、告警场景示例

### 场景1：任务连续失败告警

当某个任务连续失败 3 次时发送告警：

```json
{
  "ruleName": "任务连续失败告警",
  "ruleType": "TASK_FAIL",
  "conditionJson": "{\"type\":\"TASK_FAIL\",\"threshold\":3,\"operator\":\"GTE\"}",
  "notificationChannels": "DINGTALK,SMS",
  "notificationUsers": "oncall@company.com,13800138000"
}
```

### 场景2：队列积压预警

当队列消息数超过 10000 时发送预警：

```json
{
  "ruleName": "队列积压预警",
  "ruleType": "QUEUE_BACKLOG",
  "clusterId": 1,
  "conditionJson": "{\"type\":\"QUEUE_BACKLOG\",\"threshold\":10000,\"operator\":\"GT\"}",
  "notificationChannels": "DINGTALK,EMAIL"
}
```

### 场景3：消费者离线告警

当队列无活跃消费者时立即告警：

```json
{
  "ruleName": "消费者离线告警",
  "ruleType": "CONSUMER_OFFLINE",
  "clusterId": 1,
  "notificationChannels": "DINGTALK,SMS,EMAIL",
  "notificationUsers": "oncall@company.com,13800138000"
}
```

---

## 六、API 接口总览

### 监控接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 系统监控 | GET | `/api/monitor/system` | 获取系统监控指标 |
| 集群监控 | GET | `/api/monitor/cluster/{id}` | 获取集群监控指标 |
| 所有集群 | GET | `/api/monitor/cluster/all` | 获取所有集群监控 |
| 队列监控 | GET | `/api/monitor/cluster/{id}/queues` | 获取队列监控指标 |
| 刷新监控 | POST | `/api/monitor/refresh` | 刷新所有监控数据 |

### 告警接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建规则 | POST | `/api/alert/rule` | 创建告警规则 |
| 更新规则 | PUT | `/api/alert/rule/{id}` | 更新告警规则 |
| 规则列表 | GET | `/api/alert/rule/list` | 获取告警规则列表 |
| 启用规则 | POST | `/api/alert/rule/{id}/enable` | 启用告警规则 |
| 禁用规则 | POST | `/api/alert/rule/{id}/disable` | 禁用告警规则 |
| 删除规则 | DELETE | `/api/alert/rule/{id}` | 删除告警规则 |
| 告警记录 | GET | `/api/alert/record/page` | 查询告警记录 |
| 测试告警 | POST | `/api/alert/test` | 测试告警发送 |

---

## 七、最佳实践

### 7.1 告警规则配置建议

1. **设置合理的阈值**：避免频繁告警
2. **多渠道通知**：关键告警使用多个渠道
3. **分级告警**：INFO/WARNING/ERROR/CRITICAL
4. **避免告警风暴**：设置告警频率限制
5. **定期检查规则**：根据实际情况调整阈值

### 7.2 监控数据保留策略

- 实时数据：保留 7 天
- 小时级聚合：保留 30 天
- 天级聚合：保留 1 年

### 7.3 性能优化建议

1. **WebSocket 连接数控制**：单个实例建议不超过 1000 个连接
2. **监控数据采集频率**：根据需要调整，避免过于频繁
3. **告警去重**：相同告警在短时间内只发送一次
4. **异步发送通知**：避免阻塞主流程

---

完整 API 文档请访问：http://localhost:8080/api/swagger-ui.html
