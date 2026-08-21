package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQVhostDTO {
    private String name;
    private String description;
    private String[] tags;
    private Boolean tracing;
    private Integer clusterState;
}
