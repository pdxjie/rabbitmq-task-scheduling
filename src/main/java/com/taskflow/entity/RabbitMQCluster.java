package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * RabbitMQ 集群实体
 */
@Data
@TableName("rabbitmq_cluster")
public class RabbitMQCluster {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clusterName;

    private String connectionType;

    private String host;

    private Integer port;

    private Integer managementPort;

    private String vhost;

    private String username;

    private String password;

    private Boolean sslEnabled;

    private String sshConfig;

    private String tags;

    private String description;

    private String status;

    private Integer healthScore;

    private String rabbitmqVersion;

    private String erlangVersion;

    private LocalDateTime lastConnectTime;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean deleted;
}
