package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQNodeDTO {
    private String name;
    private String type;
    private Boolean running;
    private Long memUsed;
    private Long memLimit;
    private Double memAlarm;
    private Long diskFree;
    private Long diskFreeLimit;
    private Double diskFreeAlarm;
    private Integer fdUsed;
    private Integer fdTotal;
    private Integer socketsUsed;
    private Integer socketsTotal;
    private Integer procUsed;
    private Integer procTotal;
    private Long uptime;
    private Integer runQueue;
}
