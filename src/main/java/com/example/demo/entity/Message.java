package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long userId;
    private String content;
    private Date createTime;
    private Integer isRead;

    // 🚨 绝对核心：必须加上 @TableField(exist = false)，告诉数据库这个字段不参与 SQL 查询
    @TableField(exist = false)
    private User user;
}