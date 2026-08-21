package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQBindingDTO {
    private String source;
    private String vhost;
    private String destination;
    private String destinationType;
    private String routingKey;
    private Map<String, Object> arguments;
    private String propertiesKey;
}
