package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.Category;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private EmailService emailService;

    // 后台首页
    @GetMapping("/index")
    public String index(Model model) {
        return "admin/index";
    }

    // 1. 饼图数据：物品分类占比
    @GetMapping("/chartData")
    @ResponseBody
    public List<Map<String, Object>> getChartData() {
        List<Goods> goodsList = goodsMapper.selectList(null);
        Map<Integer, Integer> categoryCount = new HashMap<>();
        for (Goods goods : goodsList) {
            Integer catId = goods.getCategoryId();
            if (catId != null) {
                categoryCount.put(catId, categoryCount.getOrDefault(catId, 0) + 1);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : categoryCount.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "分类 " + entry.getKey());
            map.put("value", entry.getValue());
            list.add(map);
        }
        return list;
    }

    // 2. 折线图数据：近7日交易金额趋势
    @GetMapping("/chartAmountData")
    @ResponseBody
    public List<Map<String, Object>> getChartAmountData() {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            // 查询每天的订单总金额
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.apply("DATE(create_time) = {0}", date.toString());
            List<Order> orders = orderMapper.selectList(wrapper);

            BigDecimal total = BigDecimal.ZERO;
            for (Order o : orders) {
                if (o.getAmount() != null) {
                    total = total.add(o.getAmount());
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("date", date.format(formatter));
            map.put("amount", total);
            list.add(map);
        }
        return list;
    }

    // 3. 折线图数据：近7日用户增长趋势
    @GetMapping("/chartUserData")
    @ResponseBody
    public List<Map<String, Object>> getChartUserData() {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.apply("DATE(create_time) = {0}", date.toString());
            Long count = userMapper.selectCount(wrapper);

            Map<String, Object> map = new HashMap<>();
            map.put("date", date.format(formatter));
            map.put("count", count);
            list.add(map);
        }
        return list;
    }

    // 用户管理列表
    @GetMapping("/users")
    public String users(Model model) {
        List<User> userList = userMapper.selectList(null);
        model.addAttribute("users", userList);
        return "admin/users";
    }

    // 物品审核列表
    @GetMapping("/goods")
    public String goods(Model model) {
        List<Goods> goodsList = goodsMapper.selectList(null);
        model.addAttribute("goodsList", goodsList);
        return "admin/goods";
    }

    // 强制下架物品
    @PostMapping("/goods/delete")
    public String deleteGoods(@RequestParam Long id) {
        goodsMapper.deleteById(id);
        return "redirect:/admin/goods";
    }

    // 物品审核通过并发邮件
    @PostMapping("/goods/approve")
    public String approveGoods(@RequestParam Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            // 假设审核通过就是状态变为 1（在售）
            goods.setStatus(1);
            goodsMapper.updateById(goods);

            // 给卖家发邮件
            try {
                User seller = userMapper.selectById(goods.getUserId());
                if (seller != null && seller.getEmail() != null && !seller.getEmail().trim().isEmpty()) {
                    emailService.sendMail(
                            seller.getEmail(),
                            "✅ 您的物品审核已通过！",
                            "尊敬的用户，您发布的物品【" + goods.getTitle() + "】已通过管理员审核，现在已经在平台上展示啦！"
                    );
                }
            } catch (Exception e) {
                System.out.println("审核邮件发送失败：" + e.getMessage());
            }
        }
        return "redirect:/admin/goods";
    }

    // 分类管理
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryMapper.selectList(null));
        return "admin/categories";
    }

    @PostMapping("/category/add")
    public String addCategory(@RequestParam String name) {
        Category category = new Category();
        category.setName(name);
        categoryMapper.insert(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/category/delete")
    public String deleteCategory(@RequestParam Integer id) {
        categoryMapper.deleteById(id);
        return "redirect:/admin/categories";
    }
}