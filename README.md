# TaskFlow - 现代化智能 RabbitMQ 任务调度平台

## 项目简介

TaskFlow 是一个现代化、智能化的企业级 RabbitMQ 管理与任务调度平台，提供：

- 🌐 **多集群管理**：在线连接和管理多个 RabbitMQ 集群
- 🧠 **AI 智能化**：异常检测、性能优化建议、容量规划
- 📊 **可视化工作流**：拖拽式工作流设计
- 🎯 **任务调度**：支持 Cron、延迟、依赖触发
- 🔍 **链路追踪**：完整的消息追踪和日志分析
- 🔐 **企业级安全**：细粒度权限控制和操作审计

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0 + Java 21
- **数据库**: MySQL 8.0
- **缓存**: Redis 7+
- **消息队列**: RabbitMQ 3.12+
- **ORM**: MyBatis Plus 3.5.5
- **定时任务**: Quartz 2.3+
- **权限认证**: Sa-Token 1.37.0
- **分布式锁**: Redisson 3.25.2
- **API 文档**: SpringDoc OpenAPI 3

### 前端（待开发）
- React 18 + TypeScript
- Ant Design 5
- @antv/X6 (工作流编辑器)
- ECharts (图表)

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7+
- RabbitMQ 3.12+ (可选，用于测试)

### 2. 数据库初始化

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/sql/schema.sql
```

或者直接执行：
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

### 3. 配置文件

编辑 `src/main/resources/application.yml`，根据实际情况修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskflow
    username: root
    password: root
    
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动项目

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者先打包再运行
mvn clean package
java -jar target/rabbitmq-task-scheduling-1.0.0-SNAPSHOT.jar
```

### 5. 访问应用

- **API 文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/system/health
- **监控指标**: http://localhost:8080/api/actuator

## 项目结构

```
rabbitmq-task-scheduling/
├── src/main/java/com/taskflow/
│   ├── TaskFlowApplication.java          # 启动类
│   ├── common/                            # 通用类
│   │   └── Result.java                    # 统一响应结果
│   ├── config/                            # 配置类
│   │   ├── MybatisPlusConfig.java        # MyBatis Plus 配置
│   │   └── WebConfig.java                # Web 配置（CORS）
│   ├── controller/                        # 控制器
│   │   ├── RabbitMQClusterController.java # 集群管理接口
│   │   └── SystemController.java         # 系统接口
│   ├── entity/                            # 实体类
│   │   ├── RabbitMQCluster.java          # 集群实体
│   │   ├── TaskDefinition.java           # 任务定义
│   │   ├── TaskExecutionLog.java         # 执行日志
│   │   └── SysUser.java                  # 用户实体
│   ├── mapper/                            # MyBatis Mapper
│   │   ├── RabbitMQClusterMapper.java
│   │   ├── TaskDefinitionMapper.java
│   │   ├── TaskExecutionLogMapper.java
│   │   └── SysUserMapper.java
│   └── service/                           # 服务层
│       ├── RabbitMQClusterService.java
│       └── impl/
│           └── RabbitMQClusterServiceImpl.java
├── src/main/resources/
│   ├── application.yml                    # 配置文件
│   └── sql/
│       └── schema.sql                     # 数据库初始化脚本
└── pom.xml                                # Maven 配置
```

## API 接口

### 集群管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有集群 | GET | `/api/cluster/list` | 获取所有集群列表 |
| 获取活跃集群 | GET | `/api/cluster/active` | 获取状态为 ACTIVE 的集群 |
| 获取集群详情 | GET | `/api/cluster/{id}` | 根据 ID 获取集群详情 |
| 创建集群 | POST | `/api/cluster` | 创建新集群（自动测试连接） |
| 更新集群 | PUT | `/api/cluster/{id}` | 更新集群配置 |
| 删除集群 | DELETE | `/api/cluster/{id}` | 删除集群 |
| 测试连接 | POST | `/api/cluster/test` | 测试集群连接是否正常 |

### 系统管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 健康检查 | GET | `/api/system/health` | 系统健康状态 |
| 系统信息 | GET | `/api/system/info` | 系统版本和环境信息 |

## 核心功能说明

### 1. 多集群连接

支持三种连接方式：

- **DIRECT**：直接连接（适用于同网络环境）
- **SSH_TUNNEL**：SSH 隧道连接（适用于内网 RabbitMQ）
- **TLS**：TLS 加密连接（适用于安全要求高的场景）

创建集群示例：

```bash
curl -X POST http://localhost:8080/api/cluster \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "生产环境集群",
    "connectionType": "DIRECT",
    "host": "rabbitmq.prod.com",
    "port": 5672,
    "managementPort": 15672,
    "vhost": "/",
    "username": "admin",
    "password": "password",
    "tags": "生产,主集群",
    "description": "生产环境主集群"
  }'
```

### 2. 自动健康检测

系统会自动测试集群连接：
- ✅ 连接成功：状态更新为 `ACTIVE`
- ❌ 连接失败：状态更新为 `ERROR`
- 记录最后连接时间

### 3. 数据库表结构

核心表：
- `rabbitmq_cluster` - 集群配置
- `task_definition` - 任务定义
- `task_execution_log` - 执行日志
- `workflow_definition` - 工作流定义
- `sys_user` - 用户管理
- `sys_audit_log` - 操作审计
- `alert_rule` - 告警规则

## 开发计划

### Phase 1: 基础功能 ✅ (当前阶段)
- [x] 项目框架搭建
- [x] 数据库设计
- [x] 多集群连接管理
- [x] API 文档集成

### Phase 2: 任务调度 (进行中)
- [ ] 任务定义和管理
- [ ] Cron 定时任务
- [ ] 延迟任务
- [ ] 任务执行引擎

### Phase 3: 工作流编排
- [ ] DAG 工作流引擎
- [ ] 工作流可视化设计
- [ ] 任务依赖管理

### Phase 4: 监控和告警
- [ ] 实时监控大盘
- [ ] 消息链路追踪
- [ ] 智能告警
- [ ] 性能分析

### Phase 5: 智能化
- [ ] AI 异常检测
- [ ] 性能优化建议
- [ ] 容量规划

## 默认账号

- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 超级管理员

## 文档

- [产品设计方案](./PRODUCT_DESIGN_MODERN_PLATFORM.md)
- [后端性能对比](./BACKEND_PERFORMANCE_COMPARISON.md)
- [调研报告](./调研报告_RabbitMQ任务调度系统.md)

## 常见问题

### 1. 启动报错：找不到 MySQL 数据库

确保已经创建数据库并执行初始化脚本：
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

### 2. RabbitMQ 连接失败

检查：
- RabbitMQ 是否启动
- 主机地址和端口是否正确
- 用户名密码是否正确
- 防火墙是否开放端口

### 3. Redis 连接失败

确保 Redis 已启动：
```bash
redis-server
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/your-repo/taskflow
- 技术支持: support@taskflow.com
