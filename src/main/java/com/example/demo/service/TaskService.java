package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.Goods;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class TaskService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 任务 1：每月 1 号凌晨 1 点，清理超过 30 天的已下架物品
     * cron 表达式：秒 分 时 日 月 周
     * 0 0 1 1 * ? 表示每月 1 号的 1:00:00 执行
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void cleanExpiredGoods() {
        System.out.println("====== 开始执行定时任务：清理30天前下架的物品 ======");

        // 计算 30 天前的时间
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        Date date = new Date(thirtyDaysAgo);

        // 构造删除条件：状态为 2 (已下架) 且 创建时间早于 30天前
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 2)
                .lt("create_time", date);

        int deleted = goodsMapper.delete(wrapper);
        System.out.println("====== 清理完成，共清理了 " + deleted + " 条过期物品 ======");
    }

    /**
     * 任务 2：每日凌晨 0 点生成平台数据统计报表
     * cron 表达式：0 0 0 * * ? 表示每天 0:00:00 执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyStatistics() {
        System.out.println("====== 开始执行每日凌晨数据统计任务 ======");

        // 1. 统计总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 2. 统计总物品数
        Long totalGoods = goodsMapper.selectCount(null);

        // 3. 统计已售出物品数 (status = 2)
        QueryWrapper<Goods> soldWrapper = new QueryWrapper<>();
        soldWrapper.eq("status", 2);
        Long soldGoods = goodsMapper.selectCount(soldWrapper);

        // 4. 计算历史交易总金额 (把所有已售出物品的价格加起来)
        List<Goods> soldList = goodsMapper.selectList(soldWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Goods g : soldList) {
            if (g.getPrice() != null) {
                totalAmount = totalAmount.add(g.getPrice());
            }
        }

        // 打印统计报表
        System.out.println("📊 每日平台数据统计报表：");
        System.out.println("   - 平台总用户数：" + totalUsers + " 人");
        System.out.println("   - 平台总闲置物品：" + totalGoods + " 件");
        System.out.println("   - 历史成功交易： " + soldGoods + " 笔");
        System.out.println("   - 历史交易总金额：￥" + totalAmount + " 元");
        System.out.println("====== 每日数据统计任务结束 ======");
    }
}