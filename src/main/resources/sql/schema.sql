-- ====================================================
-- TaskFlow 数据库初始化脚本
-- ====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS taskflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE taskflow;

-- ====================================================
-- 1. RabbitMQ 集群管理表
-- ====================================================
CREATE TABLE `rabbitmq_cluster` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `cluster_name` VARCHAR(100) NOT NULL COMMENT '集群名称',
    `connection_type` VARCHAR(20) NOT NULL COMMENT '连接类型：DIRECT/SSH_TUNNEL/TLS',
    `host` VARCHAR(255) NOT NULL COMMENT '主机地址',
    `port` INT NOT NULL DEFAULT 5672 COMMENT 'AMQP 端口',
    `management_port` INT NOT NULL DEFAULT 15672 COMMENT '管理端口',
    `vhost` VARCHAR(255) NOT NULL DEFAULT '/' COMMENT 'Virtual Host',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `ssl_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用SSL',
    `ssh_config` JSON COMMENT 'SSH 隧道配置',
    `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
    `description` VARCHAR(500) COMMENT '描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/ERROR',
    `health_score` INT DEFAULT 0 COMMENT '健康度评分 0-100',
    `rabbitmq_version` VARCHAR(50) COMMENT 'RabbitMQ 版本',
    `erlang_version` VARCHAR(50) COMMENT 'Erlang 版本',
    `last_connect_time` DATETIME COMMENT '最后连接时间',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    INDEX `idx_cluster_name` (`cluster_name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RabbitMQ 集群配置表';

-- ====================================================
-- 2. 任务定义表
-- ====================================================
CREATE TABLE `task_definition` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型：SHELL/HTTP/RPC/CODE/MESSAGE',
    `trigger_type` VARCHAR(20) NOT NULL COMMENT '触发类型：IMMEDIATE/DELAY/CRON/DEPENDENCY',
    `cron_expression` VARCHAR(100) COMMENT 'Cron 表达式',
    `delay_seconds` INT COMMENT '延迟秒数',
    `priority` TINYINT NOT NULL DEFAULT 5 COMMENT '优先级 1-10',
    `timeout_seconds` INT NOT NULL DEFAULT 300 COMMENT '超时时间（秒）',
    `retry_count` INT NOT NULL DEFAULT 3 COMMENT '重试次数',
    `retry_strategy` VARCHAR(20) DEFAULT 'FIXED' COMMENT '重试策略：FIXED/EXPONENTIAL',
    `cluster_id` BIGINT NOT NULL COMMENT '所属集群ID',
    `exchange_name` VARCHAR(255) COMMENT '交换机名称',
    `routing_key` VARCHAR(255) COMMENT '路由键',
    `queue_name` VARCHAR(255) COMMENT '队列名称',
    `task_content` TEXT COMMENT '任务内容（脚本/URL/代码/消息体）',
    `task_params` JSON COMMENT '任务参数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED/ARCHIVED',
    `next_execute_time` DATETIME COMMENT '下次执行时间（Cron任务）',
    `last_execute_time` DATETIME COMMENT '最后执行时间',
    `execute_count` BIGINT NOT NULL DEFAULT 0 COMMENT '执行次数',
    `success_count` BIGINT NOT NULL DEFAULT 0 COMMENT '成功次数',
    `fail_count` BIGINT NOT NULL DEFAULT 0 COMMENT '失败次数',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_task_name` (`task_name`),
    INDEX `idx_cluster_id` (`cluster_id`),
    INDEX `idx_status_trigger` (`status`, `trigger_type`),
    INDEX `idx_next_execute` (`next_execute_time`),
    FOREIGN KEY (`cluster_id`) REFERENCES `rabbitmq_cluster`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务定义表';

-- ====================================================
-- 3. 任务执行日志表（按时间分表）
-- ====================================================
CREATE TABLE `task_execution_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `trace_id` VARCHAR(64) NOT NULL COMMENT '追踪ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration_ms` BIGINT COMMENT '执行耗时（毫秒）',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：PENDING/RUNNING/SUCCESS/FAILED/TIMEOUT',
    `result` TEXT COMMENT '执行结果',
    `error_message` TEXT COMMENT '错误信息',
    `error_stack` TEXT COMMENT '错误堆栈',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `worker_id` VARCHAR(100) COMMENT '执行器ID',
    `worker_ip` VARCHAR(50) COMMENT '执行器IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

-- ====================================================
-- 4. 工作流定义表
-- ====================================================
CREATE TABLE `workflow_definition` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `workflow_name` VARCHAR(100) NOT NULL COMMENT '工作流名称',
    `workflow_description` VARCHAR(500) COMMENT '工作流描述',
    `dag_json` JSON NOT NULL COMMENT 'DAG 定义（JSON格式）',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `cluster_id` BIGINT NOT NULL COMMENT '所属集群ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_workflow_name` (`workflow_name`),
    INDEX `idx_cluster_id` (`cluster_id`),
    FOREIGN KEY (`cluster_id`) REFERENCES `rabbitmq_cluster`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流定义表';

-- ====================================================
-- 5. 任务依赖关系表
-- ====================================================
CREATE TABLE `task_dependency` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `parent_task_id` BIGINT NOT NULL COMMENT '父任务ID',
    `child_task_id` BIGINT NOT NULL COMMENT '子任务ID',
    `condition` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '触发条件：SUCCESS/FAILED/ALWAYS',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_parent_child` (`parent_task_id`, `child_task_id`),
    INDEX `idx_child_task` (`child_task_id`),
    FOREIGN KEY (`parent_task_id`) REFERENCES `task_definition`(`id`),
    FOREIGN KEY (`child_task_id`) REFERENCES `task_definition`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务依赖关系表';

-- ====================================================
-- 6. 用户表
-- ====================================================
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：SUPER_ADMIN/ADMIN/USER',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ====================================================
-- 7. 操作审计日志表
-- ====================================================
CREATE TABLE `sys_audit_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT COMMENT '操作人ID',
    `username` VARCHAR(50) COMMENT '操作人用户名',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/EXECUTE',
    `resource_type` VARCHAR(50) NOT NULL COMMENT '资源类型：CLUSTER/TASK/WORKFLOW',
    `resource_id` BIGINT COMMENT '资源ID',
    `resource_name` VARCHAR(255) COMMENT '资源名称',
    `operation_detail` TEXT COMMENT '操作详情',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT 'User Agent',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_resource` (`resource_type`, `resource_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- ====================================================
-- 8. 告警规则表
-- ====================================================
CREATE TABLE `alert_rule` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：TASK_FAIL/QUEUE_BACKLOG/CONSUMER_OFFLINE',
    `cluster_id` BIGINT COMMENT '关联集群ID',
    `condition_json` JSON NOT NULL COMMENT '触发条件（JSON格式）',
    `notification_channels` VARCHAR(500) COMMENT '通知渠道：EMAIL,DINGTALK,WECHAT',
    `notification_users` VARCHAR(500) COMMENT '通知用户（逗号分隔）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- ====================================================
-- 9. 告警记录表
-- ====================================================
CREATE TABLE `alert_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `rule_id` BIGINT NOT NULL COMMENT '规则ID',
    `alert_title` VARCHAR(255) NOT NULL COMMENT '告警标题',
    `alert_content` TEXT NOT NULL COMMENT '告警内容',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别：INFO/WARNING/ERROR/CRITICAL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/FAILED',
    `send_time` DATETIME COMMENT '发送时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_rule_id` (`rule_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`rule_id`) REFERENCES `alert_rule`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- ====================================================
-- 初始化数据
-- ====================================================

-- 插入默认管理员账号（密码: admin123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM8lE9lBOsl', '系统管理员', 'admin@taskflow.com', 'SUPER_ADMIN', 'ACTIVE');

-- 插入示例集群配置
INSERT INTO `rabbitmq_cluster` (`cluster_name`, `connection_type`, `host`, `port`, `management_port`, `vhost`, `username`, `password`, `tags`, `description`, `status`)
VALUES ('本地开发集群', 'DIRECT', 'localhost', 5672, 15672, '/', 'guest', 'guest', '开发,测试', 'Docker 本地 RabbitMQ 集群', 'ACTIVE');
