package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. 定义交换机名称
    public static final String ORDER_EXCHANGE = "order.exchange";
    // 2. 定义队列名称
    public static final String ORDER_QUEUE = "order.queue";
    // 3. 定义路由键
    public static final String ORDER_ROUTING_KEY = "order.routing.key";

    // 声明交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // 声明队列
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE);
    }

    // 绑定队列到交换机
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }
}