package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendMail(String to, String subject, String content) {
        System.out.println("====== 准备发送邮件 ======");
        System.out.println("收件人: " + to);
        System.out.println("主题: " + subject);
        System.out.println("内容: " + content);
        System.out.println("=========================");

        // 真实发送逻辑
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2335415150@qq.com"); // 必须和配置文件里的 username 一致
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        // 如果这里报错，控制台会打印红色的异常堆栈
        mailSender.send(message);
        System.out.println("====== 邮件发送成功！ ======");
    }
}