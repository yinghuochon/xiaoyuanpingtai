package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wanted")
public class Wanted {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Date createTime;
}