-- 工作流执行历史表
CREATE TABLE `workflow_execution_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `workflow_id` BIGINT NOT NULL COMMENT '工作流ID',
    `instance_id` VARCHAR(64) NOT NULL COMMENT '实例ID',
    `workflow_name` VARCHAR(100) COMMENT '工作流名称',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration_ms` BIGINT COMMENT '执行耗时（毫秒）',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：RUNNING/SUCCESS/FAILED/CANCELLED/PARTIAL_SUCCESS',
    `execution_params` TEXT COMMENT '执行参数',
    `node_statuses` JSON COMMENT '节点执行状态（JSON格式）',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_workflow_id` (`workflow_id`),
    INDEX `idx_instance_id` (`instance_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`workflow_id`) REFERENCES `workflow_definition`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行历史表';
