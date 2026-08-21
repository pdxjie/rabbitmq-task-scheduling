package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitMQOverviewDTO {
    private String rabbitmqVersion;
    private String erlangVersion;
    private String clusterName;
    private ManagementVersion managementVersion;
    private ObjectTotals objectTotals;
    private QueueTotals queueTotals;
    private MessageStats messageStats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManagementVersion {
        private String managementVersion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectTotals {
        private Integer connections;
        private Integer channels;
        private Integer exchanges;
        private Integer queues;
        private Integer consumers;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QueueTotals {
        private Long messages;
        private Long messagesReady;
        private Long messagesUnacknowledged;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageStats {
        private Long publishDetails;
        private Long deliverGetDetails;
        private Long ackDetails;
    }
}
