package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goods_image")
public class GoodsImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId; // 关联的物品ID
    private String url;   // 图片路径
}