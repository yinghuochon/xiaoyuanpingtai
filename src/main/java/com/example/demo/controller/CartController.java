package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.Goods;
import com.example.demo.entity.User;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;

    // 1. 加入购物车
    @GetMapping("/add/{goodsId}")
    public String addToCart(@PathVariable Long goodsId, Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        String userId = getUserId(authentication);
        if (userId != null) {
            // 把商品ID存入 Redis 的 Hash 结构，Key为 cart:用户ID
            redisTemplate.opsForHash().put("cart:" + userId, String.valueOf(goodsId), String.valueOf(System.currentTimeMillis()));
        }
        return "redirect:/cart/my";
    }

    // 2. 查看我的购物车
    @GetMapping("/my")
    public String myCart(Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        String userId = getUserId(authentication);

        // 从 Redis 获取购物车数据
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries("cart:" + userId);
        List<Goods> cartGoods = new ArrayList<>();

        for (Object goodsId : cartMap.keySet()) {
            Goods goods = goodsMapper.selectById(Long.parseLong(goodsId.toString()));
            if (goods != null) {
                cartGoods.add(goods);
            }
        }

        model.addAttribute("cartGoods", cartGoods);
        return "cart/my"; // 对应 templates/cart/my.html
    }

    // 3. 移除购物车商品
    @GetMapping("/remove/{goodsId}")
    public String removeCart(@PathVariable Long goodsId, Authentication authentication) {
        String userId = getUserId(authentication);
        if (userId != null) {
            redisTemplate.opsForHash().delete("cart:" + userId, String.valueOf(goodsId));
        }
        return "redirect:/cart/my";
    }

    // 辅助方法：获取当前登录用户ID
    private String getUserId(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId().toString() : null;
    }
}