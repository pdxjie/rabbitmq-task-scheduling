package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQQueueDTO {
    private String name;
    private String vhost;
    private Boolean durable;
    private Boolean autoDelete;
    private Integer consumers;
    private String state;
    private Long messages;
    private Long messagesReady;
    private Long messagesUnacknowledged;
    private Long memory;
    private String node;
}
