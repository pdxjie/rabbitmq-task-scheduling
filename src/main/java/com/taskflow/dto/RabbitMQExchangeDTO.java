package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQExchangeDTO {
    private String name;
    private String vhost;
    private String type;
    private Boolean durable;
    private Boolean autoDelete;
    private Boolean internal;
}
