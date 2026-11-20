package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data // 这个注解帮你自动生成了 setUserId, setStatus 等所有方法
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer status;
    private Date createTime;

    // 🚨 下面这两个字段不存数据库，专门用来在首页展示卖家的真实信息
    @TableField(exist = false)
    private String sellerName;

    @TableField(exist = false)
    private String sellerAvatar;
}