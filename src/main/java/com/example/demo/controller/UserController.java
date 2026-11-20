package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Goods;
import com.example.demo.entity.GoodsImage;
import com.example.demo.entity.Message;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.GoodsImageMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.mapper.ReviewMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsImageMapper goodsImageMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ReviewMapper reviewMapper;

    // 注册页面
    @GetMapping("/register")
    public String registerPage() {
        return "user/register";
    }

    // 处理注册（带 JSR-303 参数校验）
    @PostMapping("/doRegister")
    public String doRegister(@Valid User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "user/register";
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            model.addAttribute("error", "该用户名已被注册，请换一个！");
            return "user/register";
        }

        // BCrypt 强哈希加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setRole("USER");
        user.setCreateTime(new java.util.Date());
        userMapper.insert(user);

        model.addAttribute("msg", "注册成功，请直接登录！");
        return "user/login";
    }

    // 个人中心
    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", username);
        User user = userMapper.selectOne(userWrapper);

        // 只查在售的物品（status = 1）
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("user_id", user.getId())
                .eq("status", 1)
                .orderByDesc("create_time");
        List<Goods> goodsList = goodsMapper.selectList(goodsWrapper);

        List<Map<String, Object>> myGoods = new ArrayList<>();
        for (Goods goods : goodsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", goods.getId());
            map.put("title", goods.getTitle());
            map.put("price", goods.getPrice());

            QueryWrapper<GoodsImage> imgWrapper = new QueryWrapper<>();
            imgWrapper.eq("goods_id", goods.getId()).last("LIMIT 1");
            GoodsImage image = goodsImageMapper.selectOne(imgWrapper);
            if (image != null) {
                map.put("imageUrl", image.getUrl());
            }
            myGoods.add(map);
        }

        // 查询该用户收到的所有评价详情
        QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
        reviewWrapper.eq("reviewee_id", user.getId()).orderByDesc("create_time");
        List<Review> reviewList = reviewMapper.selectList(reviewWrapper);

        List<Map<String, Object>> myReviews = new ArrayList<>();
        for (Review r : reviewList) {
            Map<String, Object> map = new HashMap<>();
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("createTime", r.getCreateTime());
            User reviewer = userMapper.selectById(r.getReviewerId());
            map.put("reviewerName", reviewer != null ? (reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername()) : "匿名用户");
            myReviews.add(map);
        }

        model.addAttribute("user", user);
        model.addAttribute("myGoods", myGoods);
        model.addAttribute("myReviews", myReviews);
        return "user/profile";
    }

    // 申请成为认证卖家
    @PostMapping("/user/becomeSeller")
    public String becomeSeller(Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            user.setRole("SELLER");
            userMapper.updateById(user);
            model.addAttribute("success", "恭喜您，已成功认证为卖家！现在可以发布闲置物品了。");
        }
        return profile(authentication, model);
    }

    // 修改个人信息
    @PostMapping("/user/updateProfile")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam(required = false) String email,
                                Authentication authentication,
                                Model model) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(wrapper);

        user.setNickname(nickname);
        user.setEmail(email);
        userMapper.updateById(user);

        model.addAttribute("success", "个人信息更新成功！");
        return profile(authentication, model);
    }

    // 头像上传
    @PostMapping("/user/uploadAvatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               Authentication authentication,
                               Model model) throws java.io.IOException {
        if (file.isEmpty()) {
            model.addAttribute("error", "请选择要上传的图片！");
            return profile(authentication, model);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(wrapper);

        String uploadDir = System.getProperty("user.dir") + "/upload/avatar/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(new File(uploadDir + fileName));

        user.setAvatar("/upload/avatar/" + fileName);
        userMapper.updateById(user);

        model.addAttribute("success", "头像上传成功！");
        return profile(authentication, model);
    }

    // 修改密码
    @PostMapping("/user/changePassword")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Authentication authentication,
                                 Model model) {
        String username = authentication.getName();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "原密码输入错误！");
            return profile(authentication, model);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        model.addAttribute("success", "密码修改成功，请重新登录！");
        return "redirect:/logout";
    }

    // 消息中心-物品留言列表
    @GetMapping("/user/messages")
    public String messages(Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(userWrapper);

        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("user_id", user.getId());
        List<Goods> myGoods = goodsMapper.selectList(goodsWrapper);

        List<Long> goodsIds = new ArrayList<>();
        for (Goods g : myGoods) {
            goodsIds.add(g.getId());
        }

        List<Map<String, Object>> messageList = new ArrayList<>();
        if (!goodsIds.isEmpty()) {
            QueryWrapper<Message> msgWrapper = new QueryWrapper<>();
            msgWrapper.in("goods_id", goodsIds).orderByDesc("create_time");
            List<Message> messages = messageMapper.selectList(msgWrapper);

            for (Message msg : messages) {
                Map<String, Object> map = new HashMap<>();
                map.put("content", msg.getContent());
                map.put("createTime", msg.getCreateTime());
                map.put("goodsId", msg.getGoodsId());

                User buyer = userMapper.selectById(msg.getUserId());
                map.put("buyerName", buyer != null ? (buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername()) : "匿名用户");

                Goods goods = goodsMapper.selectById(msg.getGoodsId());
                map.put("goodsTitle", goods != null ? goods.getTitle() : "已下架物品");

                messageList.add(map);
            }

            UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("goods_id", goodsIds).set("is_read", 1);
            messageMapper.update(null, updateWrapper);
        }

        model.addAttribute("messages", messageList);
        return "user/messages";
    }

    // 🚨 核心修复：未读消息数量（合并留言 + 私信）
    @GetMapping("/user/unreadCount")
    @ResponseBody
    public Result<Integer> getUnreadCount(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.success(0);
        }
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User user = userMapper.selectOne(userWrapper);
        if (user == null) return Result.success(0);

        // 1. 统计物品的未读留言
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("user_id", user.getId());
        List<Goods> myGoods = goodsMapper.selectList(goodsWrapper);
        List<Long> goodsIds = new ArrayList<>();
        for (Goods g : myGoods) {
            goodsIds.add(g.getId());
        }

        int messageCount = 0;
        if (!goodsIds.isEmpty()) {
            QueryWrapper<Message> msgWrapper = new QueryWrapper<>();
            msgWrapper.in("goods_id", goodsIds).eq("is_read", 0);
            messageCount = Math.toIntExact(messageMapper.selectCount(msgWrapper));
        }

        // 2. 统计未读的私信（包括系统通知）
        QueryWrapper<ChatMessage> chatWrapper = new QueryWrapper<>();
        chatWrapper.eq("receiver_id", user.getId()).eq("is_read", 0);
        int chatCount = Math.toIntExact(chatMessageMapper.selectCount(chatWrapper));

        // 返回总和
        return Result.success(messageCount + chatCount);
    }
}