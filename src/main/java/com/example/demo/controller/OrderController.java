package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private EmailService emailService;

    // 我的订单列表
    @GetMapping("/my")
    public String myOrders(Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(userWrapper);

        QueryWrapper<Order> buyWrapper = new QueryWrapper<>();
        buyWrapper.eq("buyer_id", user.getId()).orderByDesc("create_time");
        List<Order> buyOrders = orderMapper.selectList(buyWrapper);

        QueryWrapper<Order> sellWrapper = new QueryWrapper<>();
        sellWrapper.eq("seller_id", user.getId()).orderByDesc("create_time");
        List<Order> sellOrders = orderMapper.selectList(sellWrapper);

        List<Map<String, Object>> buyList = new ArrayList<>();
        for (Order o : buyOrders) {
            Map<String, Object> map = new HashMap<>();
            map.put("order", o);
            Goods g = goodsMapper.selectById(o.getGoodsId());
            map.put("goodsTitle", g != null ? g.getTitle() : "物品已删除");
            User seller = userMapper.selectById(o.getSellerId());
            map.put("otherName", seller != null ? seller.getUsername() : "未知卖家");
            QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
            reviewWrapper.eq("order_id", o.getId());
            map.put("hasReviewed", reviewMapper.selectCount(reviewWrapper) > 0);
            buyList.add(map);
        }

        List<Map<String, Object>> sellList = new ArrayList<>();
        for (Order o : sellOrders) {
            Map<String, Object> map = new HashMap<>();
            map.put("order", o);
            Goods g = goodsMapper.selectById(o.getGoodsId());
            map.put("goodsTitle", g != null ? g.getTitle() : "物品已删除");
            User buyer = userMapper.selectById(o.getBuyerId());
            map.put("otherName", buyer != null ? buyer.getUsername() : "未知买家");
            sellList.add(map);
        }

        model.addAttribute("buyList", buyList);
        model.addAttribute("sellList", sellList);
        return "order/my";
    }

    // 确认交易（只有卖家能点）
    @PostMapping("/confirm")
    public String confirm(@RequestParam Long orderId, Authentication authentication, RedirectAttributes redirectAttributes) {
        Order order = orderMapper.selectById(orderId);

        // 🚨 核心修复：校验当前登录用户是不是这笔订单的卖家
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User currentUser = userMapper.selectOne(userWrapper);

        if (order == null || !order.getSellerId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "操作失败：您无权处理该订单！");
            return "redirect:/order/my";
        }

        order.setStatus(2);
        orderMapper.updateById(order);

        // 给买家发站内信
        try {
            User buyer = userMapper.selectById(order.getBuyerId());
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setGoodsId(order.getGoodsId());
            chatMsg.setSenderId(order.getSellerId());
            chatMsg.setReceiverId(order.getBuyerId());
            chatMsg.setContent("【系统通知】卖家已确认交易完成！您购买的物品【" + (goods != null ? goods.getTitle() : "未知") + "】交易成功，快去个人中心给卖家点个好评吧！");
            chatMsg.setCreateTime(new Date());
            chatMsg.setIsRead(0);
            chatMessageMapper.insert(chatMsg);
        } catch (Exception e) {
            System.out.println("❌ 发送站内信失败：" + e.getMessage());
        }

        // 给买家发邮件
        try {
            User buyer = userMapper.selectById(order.getBuyerId());
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (buyer != null && buyer.getEmail() != null && !buyer.getEmail().trim().isEmpty()) {
                String emailContent = "尊敬的用户，您在平台购买的物品【"
                        + (goods != null ? goods.getTitle() : "未知物品") + "】卖家已确认交易完成！\n\n"
                        + "订单号：" + order.getOrderNo() + "\n"
                        + "成交金额：￥" + order.getAmount() + "\n\n"
                        + "感谢您的使用，快去个人中心给卖家点个好评吧！";
                emailService.sendMail(buyer.getEmail(), "🎉 交易已完成，快去评价吧！", emailContent);
            }
        } catch (Exception e) {
            System.out.println("❌ 发送交易完成邮件失败：" + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("success", "已确认交易完成！");
        return "redirect:/order/my";
    }

    // 取消订单（买家或卖家都能点，但必须身份匹配）
    @PostMapping("/cancel")
    public String cancel(@RequestParam Long orderId, Authentication authentication, RedirectAttributes redirectAttributes) {
        Order order = orderMapper.selectById(orderId);

        // 🚨 核心修复：校验当前登录用户是不是该订单的买家或卖家
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User currentUser = userMapper.selectOne(userWrapper);

        if (order == null || (!order.getBuyerId().equals(currentUser.getId()) && !order.getSellerId().equals(currentUser.getId()))) {
            redirectAttributes.addFlashAttribute("error", "操作失败：您无权取消该订单！");
            return "redirect:/order/my";
        }

        order.setStatus(3);
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(1);
            goodsMapper.updateById(goods);
        }

        redirectAttributes.addFlashAttribute("success", "订单已取消，物品已重新上架！");
        return "redirect:/order/my";
    }

    // 提交评价
    @PostMapping("/review")
    public String submitReview(@RequestParam Long orderId,
                               @RequestParam Integer rating,
                               @RequestParam String content,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) {
            redirectAttributes.addFlashAttribute("error", "订单未完成，无法评价！");
            return "redirect:/order/my";
        }

        QueryWrapper<Review> existWrapper = new QueryWrapper<>();
        existWrapper.eq("order_id", orderId);
        if (reviewMapper.selectCount(existWrapper) > 0) {
            redirectAttributes.addFlashAttribute("error", "该订单已经评价过了！");
            return "redirect:/order/my";
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User buyer = userMapper.selectOne(userWrapper);

        Review review = new Review();
        review.setOrderId(orderId);
        review.setGoodsId(order.getGoodsId());
        review.setReviewerId(buyer.getId());
        review.setRevieweeId(order.getSellerId());
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(new Date());
        reviewMapper.insert(review);

        QueryWrapper<Review> sellerWrapper = new QueryWrapper<>();
        sellerWrapper.eq("reviewee_id", order.getSellerId());
        List<Review> allReviews = reviewMapper.selectList(sellerWrapper);

        double sum = 0;
        for (Review r : allReviews) {
            sum += r.getRating();
        }
        double avg = allReviews.isEmpty() ? 5.0 : sum / allReviews.size();

        User seller = userMapper.selectById(order.getSellerId());
        seller.setRatingAvg(BigDecimal.valueOf(avg).setScale(1, BigDecimal.ROUND_HALF_UP));
        seller.setReviewCount(allReviews.size());
        userMapper.updateById(seller);

        redirectAttributes.addFlashAttribute("success", "评价成功！感谢您的反馈。");
        redirectAttributes.addFlashAttribute("success", "订单已取消，物品已重新上架！");
        return "redirect:/order/my";
    }
}