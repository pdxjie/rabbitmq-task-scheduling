package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQConnectionDTO {
    private String name;
    private String peerHost;
    private Integer peerPort;
    private String user;
    private String vhost;
    private String state;
    private Integer channels;
    private String clientProperties;
}
