package com.taskflow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.*;
import com.taskflow.entity.RabbitMQCluster;
import com.taskflow.mapper.RabbitMQClusterMapper;
import com.taskflow.service.RabbitMQManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQManagementServiceImpl implements RabbitMQManagementService {

    private final RabbitMQClusterMapper clusterMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getManagementUrl(Long clusterId) {
        RabbitMQCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new RuntimeException("集群不存在");
        }
        return String.format("http://%s:%d/api", cluster.getHost(), cluster.getManagementPort());
    }

    private HttpHeaders getAuthHeaders(Long clusterId) {
        RabbitMQCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            throw new RuntimeException("集群不存在");
        }

        HttpHeaders headers = new HttpHeaders();
        String auth = cluster.getUsername() + ":" + cluster.getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    @Override
    public List<RabbitMQQueueDTO> getAllQueues(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/queues";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQQueueDTO>>() {});
        } catch (Exception e) {
            log.error("获取队列列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQQueueDTO> getQueuesByVhost(Long clusterId, String vhost) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost);
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQQueueDTO>>() {});
        } catch (Exception e) {
            log.error("获取队列列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public RabbitMQQueueDTO getQueueDetail(Long clusterId, String vhost, String queueName) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName;
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), RabbitMQQueueDTO.class);
        } catch (Exception e) {
            log.error("获取队列详情失败", e);
            return null;
        }
    }

    @Override
    public List<RabbitMQExchangeDTO> getAllExchanges(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/exchanges";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQExchangeDTO>>() {});
        } catch (Exception e) {
            log.error("获取交换机列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQExchangeDTO> getExchangesByVhost(Long clusterId, String vhost) {
        try {
            String url = getManagementUrl(clusterId) + "/exchanges/" + encodeVhost(vhost);
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQExchangeDTO>>() {});
        } catch (Exception e) {
            log.error("获取交换机列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQMessageDTO> getMessages(Long clusterId, String vhost, String queueName, Integer count) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName + "/get";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("count", count != null ? count : 10);
            requestBody.put("ackmode", "ack_requeue_false");
            requestBody.put("encoding", "auto");

            HttpHeaders headers = getAuthHeaders(clusterId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQMessageDTO>>() {});
        } catch (Exception e) {
            log.error("获取消息失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQConnectionDTO> getAllConnections(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/connections";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQConnectionDTO>>() {});
        } catch (Exception e) {
            log.error("获取连接列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void purgeQueue(Long clusterId, String vhost, String queueName) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName + "/contents";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("队列清空成功: {}/{}", vhost, queueName);
        } catch (Exception e) {
            log.error("清空队列失败", e);
            throw new RuntimeException("清空队列失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteQueue(Long clusterId, String vhost, String queueName) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName;
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("队列删除成功: {}/{}", vhost, queueName);
        } catch (Exception e) {
            log.error("删除队列失败", e);
            throw new RuntimeException("删除队列失败: " + e.getMessage());
        }
    }

    @Override
    public RabbitMQOverviewDTO getOverview(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/overview";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), RabbitMQOverviewDTO.class);
        } catch (Exception e) {
            log.error("获取概览信息失败", e);
            return null;
        }
    }

    @Override
    public void createQueue(Long clusterId, String vhost, String queueName, Map<String, Object> config) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName;

            HttpHeaders headers = getAuthHeaders(clusterId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(config, headers);

            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            log.info("队列创建成功: {}/{}", vhost, queueName);
        } catch (Exception e) {
            log.error("创建队列失败", e);
            throw new RuntimeException("创建队列失败: " + e.getMessage());
        }
    }

    @Override
    public void createExchange(Long clusterId, String vhost, String exchangeName, Map<String, Object> config) {
        try {
            String url = getManagementUrl(clusterId) + "/exchanges/" + encodeVhost(vhost) + "/" + exchangeName;

            HttpHeaders headers = getAuthHeaders(clusterId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(config, headers);

            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            log.info("交换机创建成功: {}/{}", vhost, exchangeName);
        } catch (Exception e) {
            log.error("创建交换机失败", e);
            throw new RuntimeException("创建交换机失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteExchange(Long clusterId, String vhost, String exchangeName) {
        try {
            String url = getManagementUrl(clusterId) + "/exchanges/" + encodeVhost(vhost) + "/" + exchangeName;
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("交换机删除成功: {}/{}", vhost, exchangeName);
        } catch (Exception e) {
            log.error("删除交换机失败", e);
            throw new RuntimeException("删除交换机失败: " + e.getMessage());
        }
    }

    @Override
    public List<RabbitMQBindingDTO> getAllBindings(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/bindings";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQBindingDTO>>() {});
        } catch (Exception e) {
            log.error("获取绑定关系列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQBindingDTO> getQueueBindings(Long clusterId, String vhost, String queueName) {
        try {
            String url = getManagementUrl(clusterId) + "/queues/" + encodeVhost(vhost) + "/" + queueName + "/bindings";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQBindingDTO>>() {});
        } catch (Exception e) {
            log.error("获取队列绑定关系失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void createBinding(Long clusterId, String vhost, String source, String destination,
                             String destinationType, String routingKey, Map<String, Object> arguments) {
        try {
            String url = getManagementUrl(clusterId) + "/bindings/" + encodeVhost(vhost) +
                        "/e/" + source + "/" + destinationType.charAt(0) + "/" + destination;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("routing_key", routingKey);
            if (arguments != null) {
                requestBody.put("arguments", arguments);
            }

            HttpHeaders headers = getAuthHeaders(clusterId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("绑定关系创建成功: {} -> {}", source, destination);
        } catch (Exception e) {
            log.error("创建绑定关系失败", e);
            throw new RuntimeException("创建绑定关系失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteBinding(Long clusterId, String vhost, String source, String destination,
                             String destinationType, String propertiesKey) {
        try {
            String url = getManagementUrl(clusterId) + "/bindings/" + encodeVhost(vhost) +
                        "/e/" + source + "/" + destinationType.charAt(0) + "/" + destination + "/" + propertiesKey;
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("绑定关系删除成功");
        } catch (Exception e) {
            log.error("删除绑定关系失败", e);
            throw new RuntimeException("删除绑定关系失败: " + e.getMessage());
        }
    }

    @Override
    public void publishMessage(Long clusterId, String vhost, String exchange, String routingKey,
                              String payload, Map<String, Object> properties) {
        try {
            String url = getManagementUrl(clusterId) + "/exchanges/" + encodeVhost(vhost) + "/" + exchange + "/publish";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("routing_key", routingKey);
            requestBody.put("payload", payload);
            requestBody.put("payload_encoding", "string");
            if (properties != null) {
                requestBody.put("properties", properties);
            }

            HttpHeaders headers = getAuthHeaders(clusterId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("消息发布成功: exchange={}, routingKey={}", exchange, routingKey);
        } catch (Exception e) {
            log.error("发布消息失败", e);
            throw new RuntimeException("发布消息失败: " + e.getMessage());
        }
    }

    @Override
    public void closeConnection(Long clusterId, String connectionName) {
        try {
            String url = getManagementUrl(clusterId) + "/connections/" + connectionName;
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("连接关闭成功: {}", connectionName);
        } catch (Exception e) {
            log.error("关闭连接失败", e);
            throw new RuntimeException("关闭连接失败: " + e.getMessage());
        }
    }

    @Override
    public List<RabbitMQChannelDTO> getAllChannels(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/channels";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQChannelDTO>>() {});
        } catch (Exception e) {
            log.error("获取通道列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RabbitMQVhostDTO> getAllVhosts(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/vhosts";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQVhostDTO>>() {});
        } catch (Exception e) {
            log.error("获取虚拟主机列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void createVhost(Long clusterId, String vhost) {
        try {
            String url = getManagementUrl(clusterId) + "/vhosts/" + encodeVhost(vhost);
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            log.info("虚拟主机创建成功: {}", vhost);
        } catch (Exception e) {
            log.error("创建虚拟主机失败", e);
            throw new RuntimeException("创建虚拟主机失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteVhost(Long clusterId, String vhost) {
        try {
            String url = getManagementUrl(clusterId) + "/vhosts/" + encodeVhost(vhost);
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            log.info("虚拟主机删除成功: {}", vhost);
        } catch (Exception e) {
            log.error("删除虚拟主机失败", e);
            throw new RuntimeException("删除虚拟主机失败: " + e.getMessage());
        }
    }

    @Override
    public List<RabbitMQNodeDTO> getAllNodes(Long clusterId) {
        try {
            String url = getManagementUrl(clusterId) + "/nodes";
            HttpEntity<String> entity = new HttpEntity<>(getAuthHeaders(clusterId));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return objectMapper.readValue(response.getBody(), new TypeReference<List<RabbitMQNodeDTO>>() {});
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            return Collections.emptyList();
        }
    }

    private String encodeVhost(String vhost) {
        if ("/".equals(vhost)) {
            return "%2F";
        }
        return vhost;
    }
}
