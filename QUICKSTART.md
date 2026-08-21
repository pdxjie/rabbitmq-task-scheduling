# 快速启动指南

## 1. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本（会自动创建数据库和表）
source src/main/resources/sql/schema.sql

# 或者使用重定向
mysql -u root -p < src/main/resources/sql/schema.sql
```

## 2. 检查配置

确认 `src/main/resources/application.yml` 中的数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskflow
    username: root
    password: root
```

## 3. 启动项目

### 方式一：使用 Maven（推荐）

```bash
mvn spring-boot:run
```

### 方式二：IDEA 运行

1. 打开项目
2. 找到 `TaskFlowApplication.java`
3. 右键 -> Run 'TaskFlowApplication'

### 方式三：打包后运行

```bash
mvn clean package
java -jar target/rabbitmq-task-scheduling-1.0.0-SNAPSHOT.jar
```

## 4. 验证启动

启动成功后访问：

- **API 文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/system/health
- **系统信息**: http://localhost:8080/api/system/info

## 5. 测试接口

### 健康检查

```bash
curl http://localhost:8080/api/system/health
```

### 创建 RabbitMQ 集群连接

```bash
curl -X POST http://localhost:8080/api/cluster \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "本地测试集群",
    "connectionType": "DIRECT",
    "host": "localhost",
    "port": 5672,
    "managementPort": 15672,
    "vhost": "/",
    "username": "guest",
    "password": "guest",
    "tags": "测试",
    "description": "本地 Docker RabbitMQ"
  }'
```

### 获取所有集群

```bash
curl http://localhost:8080/api/cluster/list
```

## 常见问题

### 1. 端口冲突

如果 8080 端口被占用，修改 `application.yml`：

```yaml
server:
  port: 8081  # 改成其他端口
```

### 2. MySQL 连接失败

检查：
- MySQL 是否启动
- 用户名密码是否正确
- 是否已创建 `taskflow` 数据库

### 3. Redis 连接失败（可选）

Redis 暂时不影响启动，如需使用：

```bash
# 启动 Redis
redis-server
```

## 下一步

项目启动成功后，可以：

1. 访问 Swagger 文档查看所有 API
2. 连接你的 RabbitMQ 集群
3. 创建任务定义（功能开发中）
4. 设计工作流（功能开发中）

## 需要帮助？

查看完整文档：[README.md](./README.md)
