package com.taskflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 延迟队列配置 ====================

    /**
     * 死信交换机（用于延迟任务）
     */
    public static final String DLX_EXCHANGE = "taskflow.dlx.exchange";

    /**
     * 延迟队列（不同延迟级别）
     */
    public static final String DELAY_QUEUE_5S = "taskflow.delay.5s";
    public static final String DELAY_QUEUE_30S = "taskflow.delay.30s";
    public static final String DELAY_QUEUE_1M = "taskflow.delay.1m";
    public static final String DELAY_QUEUE_5M = "taskflow.delay.5m";
    public static final String DELAY_QUEUE_30M = "taskflow.delay.30m";
    public static final String DELAY_QUEUE_1H = "taskflow.delay.1h";

    /**
     * 实际执行队列
     */
    public static final String EXECUTE_QUEUE = "taskflow.execute.queue";
    public static final String EXECUTE_EXCHANGE = "taskflow.execute.exchange";

    /**
     * 消息转换器（JSON）
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // 消息发送确认
        template.setMandatory(true);
        return template;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ==================== 死信交换机 ====================

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ==================== 执行队列和交换机 ====================

    @Bean
    public Queue executeQueue() {
        return QueueBuilder.durable(EXECUTE_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange executeExchange() {
        return new DirectExchange(EXECUTE_EXCHANGE, true, false);
    }

    @Bean
    public Binding executeBinding() {
        return BindingBuilder
                .bind(executeQueue())
                .to(executeExchange())
                .with("execute");
    }

    // ==================== 延迟队列（5秒）====================

    @Bean
    public Queue delayQueue5s() {
        return QueueBuilder.durable(DELAY_QUEUE_5S)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 5000) // 5秒
                .build();
    }

    // ==================== 延迟队列（30秒）====================

    @Bean
    public Queue delayQueue30s() {
        return QueueBuilder.durable(DELAY_QUEUE_30S)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 30000) // 30秒
                .build();
    }

    // ==================== 延迟队列（1分钟）====================

    @Bean
    public Queue delayQueue1m() {
        return QueueBuilder.durable(DELAY_QUEUE_1M)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 60000) // 1分钟
                .build();
    }

    // ==================== 延迟队列（5分钟）====================

    @Bean
    public Queue delayQueue5m() {
        return QueueBuilder.durable(DELAY_QUEUE_5M)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 300000) // 5分钟
                .build();
    }

    // ==================== 延迟队列（30分钟）====================

    @Bean
    public Queue delayQueue30m() {
        return QueueBuilder.durable(DELAY_QUEUE_30M)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 1800000) // 30分钟
                .build();
    }

    // ==================== 延迟队列（1小时）====================

    @Bean
    public Queue delayQueue1h() {
        return QueueBuilder.durable(DELAY_QUEUE_1H)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "execute")
                .withArgument("x-message-ttl", 3600000) // 1小时
                .build();
    }

    // ==================== 死信路由到执行队列 ====================

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(executeQueue())
                .to(dlxExchange())
                .with("execute");
    }
}
