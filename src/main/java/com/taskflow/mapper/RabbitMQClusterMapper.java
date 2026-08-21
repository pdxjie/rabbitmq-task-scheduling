package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.RabbitMQCluster;
import org.apache.ibatis.annotations.Mapper;

/**
 * RabbitMQ 集群 Mapper
 */
@Mapper
public interface RabbitMQClusterMapper extends BaseMapper<RabbitMQCluster> {
}
