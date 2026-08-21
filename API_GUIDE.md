# TaskFlow API 使用指南

## 一、快速开始

### 1. 启动项目

```bash
# 初始化数据库
mysql -u root -p < src/main/resources/sql/schema.sql

# 启动项目
mvn spring-boot:run
```

### 2. 访问 API 文档

http://localhost:8080/api/swagger-ui.html

---

## 二、核心功能使用

### 1. 集群管理

#### 1.1 连接 RabbitMQ 集群

```bash
curl -X POST http://localhost:8080/api/cluster \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "本地开发集群",
    "connectionType": "DIRECT",
    "host": "localhost",
    "port": 5672,
    "managementPort": 15672,
    "vhost": "/",
    "username": "guest",
    "password": "guest",
    "tags": "开发,测试",
    "description": "本地 Docker RabbitMQ"
  }'
```

#### 1.2 获取所有集群

```bash
curl http://localhost:8080/api/cluster/list
```

#### 1.3 测试集群连接

```bash
curl -X POST http://localhost:8080/api/cluster/test \
  -H "Content-Type: application/json" \
  -d '{
    "host": "localhost",
    "port": 5672,
    "vhost": "/",
    "username": "guest",
    "password": "guest"
  }'
```

---

### 2. 任务管理

#### 2.1 创建立即执行任务（发送消息）

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "发送订单消息",
    "taskType": "MESSAGE",
    "triggerType": "IMMEDIATE",
    "clusterId": 1,
    "queueName": "test.queue",
    "taskContent": "{ \"orderId\": \"123456\", \"amount\": 99.99 }",
    "priority": 5,
    "retryCount": 3,
    "timeoutSeconds": 60
  }'
```

#### 2.2 创建 Cron 定时任务

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "每分钟发送心跳",
    "taskType": "MESSAGE",
    "triggerType": "CRON",
    "cronExpression": "0 * * * * ?",
    "clusterId": 1,
    "queueName": "heartbeat.queue",
    "taskContent": "{ \"type\": \"heartbeat\", \"timestamp\": \"${timestamp}\" }",
    "priority": 5,
    "retryCount": 3
  }'
```

**常用 Cron 表达式**：
- `0 * * * * ?` - 每分钟执行
- `0 */5 * * * ?` - 每 5 分钟执行
- `0 0 * * * ?` - 每小时执行
- `0 0 0 * * ?` - 每天 0 点执行
- `0 0 9 * * ?` - 每天上午 9 点执行
- `0 0 9 ? * MON-FRI` - 工作日上午 9 点执行

#### 2.3 创建延迟任务

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "30秒后发送通知",
    "taskType": "MESSAGE",
    "triggerType": "DELAY",
    "delaySeconds": 30,
    "clusterId": 1,
    "queueName": "notification.queue",
    "taskContent": "{ \"message\": \"这是一条延迟消息\" }",
    "priority": 5,
    "retryCount": 3
  }'
```

**支持的延迟级别**：
- 5 秒、30 秒、1 分钟、5 分钟、30 分钟、1 小时

#### 2.4 创建 HTTP 请求任务

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "调用外部API",
    "taskType": "HTTP",
    "triggerType": "CRON",
    "cronExpression": "0 */10 * * * ?",
    "clusterId": 1,
    "taskContent": "https://api.example.com/sync",
    "timeoutSeconds": 30,
    "retryCount": 3
  }'
```

#### 2.5 创建 Shell 脚本任务

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "数据备份脚本",
    "taskType": "SHELL",
    "triggerType": "CRON",
    "cronExpression": "0 0 2 * * ?",
    "clusterId": 1,
    "taskContent": "#!/bin/bash\necho \"开始备份\"\nmysqldump -u root -p database > backup.sql\necho \"备份完成\"",
    "timeoutSeconds": 300
  }'
```

#### 2.6 立即执行任务

```bash
curl -X POST http://localhost:8080/api/task/1/execute
```

#### 2.7 启用/禁用任务

```bash
# 启用任务
curl -X POST http://localhost:8080/api/task/1/enable

# 禁用任务
curl -X POST http://localhost:8080/api/task/1/disable
```

#### 2.8 获取任务列表

```bash
# 获取所有任务
curl http://localhost:8080/api/task/list

# 按集群过滤
curl "http://localhost:8080/api/task/list?clusterId=1"

# 按状态过滤
curl "http://localhost:8080/api/task/list?status=ENABLED"

# 分页查询
curl "http://localhost:8080/api/task/page?current=1&size=10&clusterId=1"
```

#### 2.9 查看任务执行日志

```bash
# 获取指定任务的执行日志
curl "http://localhost:8080/api/task/1/logs?current=1&size=20"

# 根据 TraceId 查询日志
curl http://localhost:8080/api/task/trace/abc123xyz
```

---

### 3. Cron 表达式工具

#### 3.1 验证 Cron 表达式

```bash
curl -X POST "http://localhost:8080/api/cron/validate?expression=0%20*%20*%20*%20*%20?"
```

#### 3.2 获取 Cron 示例

```bash
curl http://localhost:8080/api/cron/examples
```

#### 3.3 查看 Cron 帮助

```bash
curl http://localhost:8080/api/cron/help
```

---

### 4. 统计分析

#### 4.1 获取任务统计

```bash
curl http://localhost:8080/api/statistics/task
```

返回数据包括：
- 任务总数、启用/禁用数量
- 按触发类型统计（Cron/延迟/立即）
- 今日执行次数、成功/失败数量
- 成功率

---

## 三、使用场景示例

### 场景1：定时数据同步

```bash
# 每天凌晨 2 点同步数据
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "每日数据同步",
    "taskType": "HTTP",
    "triggerType": "CRON",
    "cronExpression": "0 0 2 * * ?",
    "clusterId": 1,
    "taskContent": "https://api.example.com/data/sync",
    "timeoutSeconds": 600,
    "retryCount": 3
  }'
```

### 场景2：订单超时自动取消

```bash
# 创建订单时发送 30 分钟延迟任务
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "订单超时检查",
    "taskType": "MESSAGE",
    "triggerType": "DELAY",
    "delaySeconds": 1800,
    "clusterId": 1,
    "queueName": "order.timeout.queue",
    "taskContent": "{ \"orderId\": \"ORD123456\" }"
  }'
```

### 场景3：心跳监控

```bash
# 每分钟发送心跳
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "系统心跳",
    "taskType": "MESSAGE",
    "triggerType": "CRON",
    "cronExpression": "0 * * * * ?",
    "clusterId": 1,
    "queueName": "system.heartbeat",
    "taskContent": "{ \"service\": \"api-server\", \"status\": \"alive\" }",
    "priority": 8
  }'
```

### 场景4：报表生成

```bash
# 每周一上午 9 点生成周报
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "生成周报",
    "taskType": "SHELL",
    "triggerType": "CRON",
    "cronExpression": "0 0 9 ? * MON",
    "clusterId": 1,
    "taskContent": "#!/bin/bash\npython /scripts/generate_report.py --type=weekly",
    "timeoutSeconds": 1800
  }'
```

---

## 四、工作流程

### 1. Cron 定时任务流程

```
1. 创建任务（设置 Cron 表达式）
2. 系统自动计算下次执行时间
3. CronTaskScheduler 每 10 秒扫描一次
4. 发现到期任务 → 异步执行
5. 执行完成 → 记录日志 → 更新统计
6. 计算下次执行时间 → 循环
```

### 2. 延迟任务流程

```
1. 创建延迟任务
2. 发送消息到延迟队列（根据延迟时间选择队列）
3. 消息在队列中等待（TTL 过期）
4. 过期后路由到死信交换机
5. 死信交换机路由到执行队列
6. DelayTaskConsumer 消费消息
7. 执行任务 → 记录日志
```

### 3. 立即执行任务流程

```
1. 创建/触发任务
2. 异步执行器执行任务
3. 记录开始日志
4. 执行任务逻辑（MESSAGE/HTTP/SHELL）
5. 成功 → 记录成功日志
6. 失败 → 记录失败日志 + 重试（如果配置）
```

---

## 五、注意事项

### 1. RabbitMQ 要求

- 确保 RabbitMQ 已启动
- 需要管理员权限创建队列和交换机
- 延迟任务需要提前声明延迟队列

### 2. 性能建议

- Cron 任务不宜过多（建议 < 1000 个）
- 扫描间隔根据需求调整（默认 10 秒）
- 任务执行使用异步线程池（默认最大 50 线程）
- 长时间任务设置合理的超时时间

### 3. 安全建议

- 生产环境修改默认密码
- Shell 任务需要严格控制权限
- HTTP 任务注意 SSRF 风险
- 消息内容避免包含敏感信息

---

## 六、故障排查

### 1. 任务未执行

- 检查任务状态是否为 ENABLED
- 查看 Cron 表达式是否正确
- 检查下次执行时间是否正确
- 查看执行日志是否有错误

### 2. 延迟任务未触发

- 确认 RabbitMQ 延迟队列已创建
- 检查消息是否成功发送
- 查看 DelayTaskConsumer 是否正常消费
- 检查 RabbitMQ 管理界面的队列状态

### 3. 任务执行失败

- 查看任务执行日志的错误信息
- 检查 RabbitMQ 连接是否正常
- 验证 HTTP 请求的 URL 是否可访问
- Shell 脚本检查语法和权限

---

## 七、API 认证（待实现）

目前 API 未启用认证，生产环境建议：

1. 启用 Sa-Token 认证
2. 实现 API Key 机制
3. 配置 IP 白名单
4. 启用操作审计日志

---

完整 API 文档请访问：http://localhost:8080/api/swagger-ui.html
