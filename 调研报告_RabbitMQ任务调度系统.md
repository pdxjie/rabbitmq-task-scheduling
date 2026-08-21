# RabbitMQ 任务调度系统调研报告

## 一、项目背景

### 1.1 RabbitMQ Management Plugin 的不足

#### 功能层面
- ❌ 无任务调度能力（延迟任务、定时任务、Cron）
- ❌ 缺乏工作流编排
- ❌ 无消息重试和死信可视化管理
- ❌ 不支持任务依赖和条件执行
- ❌ 缺乏业务层抽象（只有队列、交换机等底层概念）

#### 体验层面
- ❌ 界面陈旧，操作繁琐
- ❌ 权限管理粒度粗
- ❌ 无中文支持
- ❌ 批量操作困难

#### 运维层面
- ❌ 监控数据有限，无主动告警
- ❌ 消息链路追踪困难
- ❌ 配置无版本管理
- ❌ 多环境管理不便

---

## 二、市场现状分析

### 2.1 类似产品对比

| 产品 | 优势 | 劣势 | 适用场景 |
|------|------|------|----------|
| **XXL-JOB** | 轻量级、Java生态、分布式调度 | 不基于消息队列、功能相对简单 | 定时任务调度 |
| **Elastic-Job** | 分布式弹性调度、高可用 | 依赖Zookeeper、学习成本高 | 大规模分布式任务 |
| **Celery (Python)** | 成熟、生态丰富、支持多种broker | 语言限制、运维复杂 | Python 技术栈 |
| **Quartz** | Java标准、功能完善 | 非分布式（需扩展）、无UI | 传统Java应用 |
| **Apache DolphinScheduler** | 功能强大、工作流可视化 | 重量级、上手成本高 | 大数据任务调度 |

### 2.2 基于 RabbitMQ 的优势

✅ **已有基础设施**：公司已使用RabbitMQ，无需引入新中间件  
✅ **高可靠性**：利用RabbitMQ的消息持久化、ACK机制  
✅ **延迟队列**：通过TTL+死信实现延迟任务  
✅ **优先级队列**：原生支持消息优先级  
✅ **消息路由**：灵活的交换机路由能力  

---

## 三、技术方案设计

### 3.1 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                     前端 Web UI                          │
│  (React/Vue + Ant Design + 可视化工作流编辑器)           │
└────────────────────┬────────────────────────────────────┘
                     │ REST API / WebSocket
┌────────────────────▼────────────────────────────────────┐
│                  后端服务层 (Spring Boot / Node.js)       │
├─────────────────────────────────────────────────────────┤
│  任务管理  │  调度引擎  │  监控告警  │  权限管理         │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                    RabbitMQ 集群                         │
├─────────────────────────────────────────────────────────┤
│  • 立即执行队列  (Immediate Queue)                       │
│  • 延迟队列      (Delay Queue + TTL + DLX)              │
│  • 优先级队列    (Priority Queue)                        │
│  • 死信队列      (Dead Letter Queue)                    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                   任务执行器 (Workers)                    │
│  支持多语言 SDK: Java / Python / Go / Node.js           │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              数据存储层                                   │
├─────────────────────────────────────────────────────────┤
│  • MySQL/PostgreSQL (任务配置、执行历史、用户权限)       │
│  • Redis (分布式锁、调度状态缓存)                        │
│  • InfluxDB/Prometheus (监控指标时序数据)                │
└─────────────────────────────────────────────────────────┘
```

### 3.2 核心功能模块

#### 1. 任务管理模块
- **任务定义**：支持Shell脚本、HTTP请求、RPC调用、自定义代码
- **触发方式**：
  - 立即执行
  - 延迟执行（指定延迟时间）
  - Cron 定时表达式
  - 依赖触发（上游任务完成后执行）
  - API 手动触发
- **任务配置**：
  - 重试次数和策略（固定延迟、指数退避）
  - 超时时间
  - 并发控制
  - 优先级

#### 2. 调度引擎
- **定时调度器**：基于Cron表达式的定时任务触发
- **延迟调度器**：利用RabbitMQ的TTL+死信实现
- **依赖调度器**：DAG（有向无环图）任务依赖管理
- **负载均衡**：多个Worker的任务分发

#### 3. 工作流编排
```
任务A (成功) ──→ 任务B ──→ 任务D
            ↓            ↗
       任务C (并行) ────┘
```
- 支持串行、并行、条件分支
- 可视化拖拽式工作流设计
- 工作流模板和版本管理

#### 4. 监控告警
- **实时监控**：
  - 队列积压数量
  - 任务执行TPS
  - 成功率/失败率
  - 平均执行时长
- **告警规则**：
  - 任务失败超过N次
  - 队列积压超过阈值
  - 执行时间超时
  - Worker离线
- **通知渠道**：邮件、钉钉、企业微信、短信

#### 5. 消息追踪
- **全链路追踪**：TraceId贯穿任务生命周期
- **执行日志**：实时查看Worker执行日志
- **消息轨迹**：发布 → 路由 → 消费 → 确认/拒绝

### 3.3 RabbitMQ 队列设计

```
# 1. 立即执行队列
task.immediate.{priority}
- priority: high / medium / low

# 2. 延迟队列（TTL + 死信）
task.delay.{delay_level}  (TTL队列)
  ↓ (过期后)
task.delay.dlx  (死信交换机)
  ↓
task.immediate.medium  (实际执行队列)

# 3. 定时任务临时队列
task.cron.temp.{task_id}

# 4. 死信队列（失败重试）
task.retry.{retry_count}
  - retry_count: 1, 2, 3

task.dead_letter.final  (最终失败队列，人工介入)
```

### 3.4 技术栈建议

#### 后端
- **Java 方案**：Spring Boot + MyBatis-Plus + RabbitMQ Client
- **Node.js 方案**：NestJS + TypeORM + amqplib
- **Go 方案**：Gin + GORM + amqp091-go

#### 前端
- **框架**：React 18 + TypeScript
- **UI 库**：Ant Design 5
- **工作流编辑器**：@antv/X6 或 React Flow
- **图表**：ECharts 或 Recharts
- **状态管理**：Zustand 或 Jotai

#### 数据库
- **关系型**：PostgreSQL（推荐）或 MySQL
- **缓存**：Redis 7+
- **时序数据**：InfluxDB 2.x 或 Prometheus

---

## 四、核心技术实现

### 4.1 延迟任务实现（RabbitMQ）

```java
// 发送延迟消息
public void sendDelayMessage(Task task, long delaySeconds) {
    rabbitTemplate.convertAndSend(
        "task.delay.exchange",
        "task.delay." + getDelayLevel(delaySeconds),
        task,
        message -> {
            message.getMessageProperties()
                .setExpiration(String.valueOf(delaySeconds * 1000));
            return message;
        }
    );
}

// 队列配置（死信路由）
@Bean
public Queue delayQueue() {
    return QueueBuilder.durable("task.delay.60s")
        .withArgument("x-dead-letter-exchange", "task.dlx")
        .withArgument("x-dead-letter-routing-key", "task.execute")
        .build();
}
```

### 4.2 Cron 定时任务实现

```java
// 使用 Spring Scheduled 或 Quartz 触发
@Scheduled(cron = "0 */5 * * * ?")  // 动态加载数据库中的Cron配置
public void scanCronTasks() {
    List<Task> tasks = taskService.getDueCronTasks();
    for (Task task : tasks) {
        // 发送消息到 RabbitMQ
        taskProducer.send(task);
        // 计算下次执行时间
        task.setNextExecuteTime(calculateNext(task.getCronExpression()));
        taskService.updateNextTime(task);
    }
}
```

### 4.3 任务依赖和工作流实现

```typescript
// DAG 执行引擎（伪代码）
class WorkflowEngine {
  async execute(workflow: Workflow) {
    const dag = buildDAG(workflow.tasks);
    const completed = new Set<string>();
    
    while (completed.size < dag.nodes.length) {
      // 找到所有依赖已完成的节点
      const ready = dag.nodes.filter(node => 
        node.dependencies.every(dep => completed.has(dep))
      );
      
      // 并行执行
      await Promise.all(
        ready.map(node => this.executeTask(node.task))
      );
      
      ready.forEach(node => completed.add(node.id));
    }
  }
}
```

### 4.4 分布式锁（防止重复执行）

```java
@Aspect
public class DistributedLockAspect {
    
    @Around("@annotation(ScheduledTask)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String lockKey = "task:lock:" + task.getId();
        
        // Redis 分布式锁
        boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        
        if (!acquired) {
            log.warn("Task {} is already running", task.getId());
            return null;
        }
        
        try {
            return pjp.proceed();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
```

---

## 五、数据库设计（核心表）

```sql
-- 任务定义表
CREATE TABLE task_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    type VARCHAR(20) NOT NULL COMMENT '任务类型：SHELL/HTTP/RPC/CODE',
    trigger_type VARCHAR(20) NOT NULL COMMENT 'IMMEDIATE/DELAY/CRON/DEPENDENCY',
    cron_expression VARCHAR(50) COMMENT 'Cron表达式',
    delay_seconds INT COMMENT '延迟秒数',
    priority TINYINT DEFAULT 5 COMMENT '优先级 1-10',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    timeout_seconds INT DEFAULT 300 COMMENT '超时时间',
    content TEXT COMMENT '任务内容（脚本/URL/代码）',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_type (status, trigger_type)
);

-- 任务执行历史表（分表）
CREATE TABLE task_execution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    trace_id VARCHAR(64) NOT NULL COMMENT '追踪ID',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
    result TEXT COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0,
    worker_id VARCHAR(50) COMMENT '执行器ID',
    INDEX idx_task_id (task_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_start_time (start_time)
) PARTITION BY RANGE (YEAR(start_time)) (
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027)
);

-- 工作流定义表
CREATE TABLE workflow_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    dag_json JSON COMMENT 'DAG定义',
    version INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ENABLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 任务依赖关系表
CREATE TABLE task_dependency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_task_id BIGINT NOT NULL,
    child_task_id BIGINT NOT NULL,
    condition VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED/ALWAYS',
    UNIQUE KEY uk_parent_child (parent_task_id, child_task_id)
);
```

---

## 六、开发计划建议

### Phase 1: MVP（4-6周）
- ✅ 基础任务管理（CRUD）
- ✅ 立即执行和延迟执行
- ✅ 简单的Web UI
- ✅ 任务执行日志查看
- ✅ 基于RabbitMQ的消息发送和消费

### Phase 2: 核心功能（6-8周）
- ✅ Cron定时任务
- ✅ 任务重试和死信处理
- ✅ 优先级队列
- ✅ 基础监控大盘
- ✅ 告警通知（钉钉/邮件）

### Phase 3: 高级特性（8-10周）
- ✅ 工作流编排（DAG）
- ✅ 可视化工作流编辑器
- ✅ 消息链路追踪
- ✅ 权限管理和多租户
- ✅ API开放平台

### Phase 4: 优化增强（持续）
- ✅ 性能优化
- ✅ 高可用和容灾
- ✅ 更多Worker SDK（多语言）
- ✅ 智能告警和异常分析

---

## 七、竞争优势

与开源产品相比：
1. **深度集成**：专为RabbitMQ优化，无需额外中间件
2. **轻量级**：不像DolphinScheduler那样重，学习成本低
3. **现代化UI**：比XXL-JOB更美观易用
4. **工作流可视化**：拖拽式设计，降低使用门槛
5. **中文友好**：完整的中文文档和界面

---

## 八、风险和挑战

### 技术风险
- ❗ RabbitMQ延迟队列精度有限（秒级，不支持毫秒）
- ❗ 大量定时任务扫描可能有性能瓶颈
- ❗ 复杂DAG执行的状态管理和死锁检测

### 解决方案
- 使用RabbitMQ Delayed Message Plugin提升延迟精度
- Cron任务分片扫描，避免单点瓶颈
- 实现严格的DAG校验和超时保护

---

## 九、参考资料

- [RabbitMQ Delayed Message Plugin](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)
- [XXL-JOB 官方文档](https://www.xuxueli.com/xxl-job/)
- [Apache DolphinScheduler](https://dolphinscheduler.apache.org/)
- [Celery 分布式任务队列](https://docs.celeryq.dev/)
- [Temporal 工作流引擎](https://temporal.io/)

---

## 十、下一步行动

1. **技术选型确认**：确定后端语言（Java/Node.js/Go）
2. **原型设计**：画出核心页面原型图
3. **技术预研**：搭建RabbitMQ环境，验证延迟队列方案
4. **需求细化**：与业务方确认核心功能优先级
5. **组建团队**：前端1人 + 后端2人 + 测试1人

---

**联系方式**：有任何问题欢迎讨论  
**文档版本**：V1.0  
**更新日期**：2026-08-21
