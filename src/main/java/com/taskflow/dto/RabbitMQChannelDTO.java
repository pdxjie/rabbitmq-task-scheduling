package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQChannelDTO {
    private String name;
    private String connectionName;
    private String vhost;
    private String user;
    private String state;
    private Integer number;
    private Integer consumerCount;
    private Integer messagesUnacknowledged;
    private Integer messagesUnconfirmed;
    private Integer prefetchCount;
    private Boolean confirm;
    private Boolean transactional;
}
