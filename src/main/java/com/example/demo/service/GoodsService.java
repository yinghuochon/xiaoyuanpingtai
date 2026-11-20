package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Goods;
import com.example.demo.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    // 🚨 缓存 key 加上了 categoryId，防止不同分类读到同一个缓存
    @Cacheable(value = "goodsList", key = "#pageNum + '-' + #pageSize + '-' + (#keyword == null ? 'all' : #keyword) + '-' + (#minPrice == null ? 'all' : #minPrice) + '-' + (#maxPrice == null ? 'all' : #maxPrice) + '-' + (#sort == null ? 'newest' : #sort) + '-' + (#categoryId == null ? 'all' : #categoryId)")
    public Page<Goods> getGoodsPage(Integer pageNum, Integer pageSize, String keyword, String minPrice, String maxPrice, String sort, Integer categoryId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只查在售的

        // 1. 关键词
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("title", keyword);
        }

        // 2. 价格区间
        if (minPrice != null && !minPrice.isEmpty()) {
            queryWrapper.ge("price", new BigDecimal(minPrice));
        }
        if (maxPrice != null && !maxPrice.isEmpty()) {
            queryWrapper.le("price", new BigDecimal(maxPrice));
        }

        // 🚨 3. 分类筛选
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }

        // 4. 排序
        if ("priceAsc".equals(sort)) {
            queryWrapper.orderByAsc("price");
        } else if ("priceDesc".equals(sort)) {
            queryWrapper.orderByDesc("price");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        return goodsMapper.selectPage(page, queryWrapper);
    }
}