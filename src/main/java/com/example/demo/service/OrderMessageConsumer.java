package com.example.demo.service;

import com.example.demo.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageConsumer {

    @Autowired
    private EmailService emailService;

    // 监听队列
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void receiveOrderMessage(String message) {
        System.out.println("🐰 RabbitMQ 消费者收到订单消息：" + message);

        // 异步发送邮件通知管理员或卖家
        try {
            // 这里为了演示，发给你的 QQ 邮箱
            emailService.sendMail(
                    "2335415150@qq.com",
                    "【RabbitMQ 异步通知】平台有新订单啦",
                    "订单详情：\n\n" + message
            );
        } catch (Exception e) {
            System.out.println("消费者发送邮件失败：" + e.getMessage());
        }
    }
}