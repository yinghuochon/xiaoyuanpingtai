package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Date createTime;

    // 🚨 核心修复：加上这个字段，Lombok 就会自动生成 setIsRead() 方法
    private Integer isRead;
}