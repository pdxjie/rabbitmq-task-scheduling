package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQMessageDTO {
    private String payload;
    private Long payloadBytes;
    private Boolean redelivered;
    private String exchange;
    private String routingKey;
    private Integer messageCount;
    private Map<String, Object> properties;
    private String payloadEncoding;
}
