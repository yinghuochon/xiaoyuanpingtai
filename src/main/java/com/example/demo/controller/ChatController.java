package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Goods;
import com.example.demo.entity.User;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;

    // 进入聊天页面
    @GetMapping("/room/{goodsId}/{sellerId}")
    public String chatRoom(@PathVariable Long goodsId, @PathVariable Long sellerId, Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        // 当前登录的用户
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User currentUser = userMapper.selectOne(userWrapper);

        Goods goods = goodsMapper.selectById(goodsId);
        User seller = userMapper.selectById(sellerId);

        // 如果是自己点自己的商品，直接返回详情页
        if (currentUser.getId().equals(sellerId)) {
            return "redirect:/goods/detail/" + goodsId;
        }

        // 查询两人关于这个商品的历史聊天记录
        QueryWrapper<ChatMessage> chatWrapper = new QueryWrapper<>();
        chatWrapper.eq("goods_id", goodsId)
                .and(w -> w.eq("sender_id", currentUser.getId()).eq("receiver_id", sellerId)
                        .or()
                        .eq("sender_id", sellerId).eq("receiver_id", currentUser.getId()))
                .orderByAsc("create_time");

        List<ChatMessage> messages = chatMessageMapper.selectList(chatWrapper);

        // 进入聊天室时，顺便把发给我的消息标记为已读
        UpdateWrapper<ChatMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("goods_id", goodsId)
                .eq("receiver_id", currentUser.getId())
                .set("is_read", 1);
        chatMessageMapper.update(null, updateWrapper);

        model.addAttribute("goods", goods);
        model.addAttribute("seller", seller);
        model.addAttribute("currentUser", currentUser); // 🚨 传递当前用户给前端
        model.addAttribute("messages", messages);
        return "chat/room";
    }

    // 发送消息
    @PostMapping("/send")
    public String sendMessage(@RequestParam Long goodsId,
                              @RequestParam Long receiverId,
                              @RequestParam String content,
                              Authentication authentication) {
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User sender = userMapper.selectOne(userWrapper);

        ChatMessage msg = new ChatMessage();
        msg.setGoodsId(goodsId);
        msg.setSenderId(sender.getId());
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setCreateTime(new Date());
        msg.setIsRead(0); // 发送时默认为未读
        chatMessageMapper.insert(msg);

        return "redirect:/chat/room/" + goodsId + "/" + receiverId;
    }

    // 我收到的私信列表
    @GetMapping("/list")
    public String chatList(Authentication authentication, Model model) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", authentication.getName());
        User currentUser = userMapper.selectOne(userWrapper);

        QueryWrapper<ChatMessage> chatWrapper = new QueryWrapper<>();
        chatWrapper.eq("receiver_id", currentUser.getId()).orderByDesc("create_time");
        List<ChatMessage> messages = chatMessageMapper.selectList(chatWrapper);

        List<Map<String, Object>> chatList = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("goodsId", msg.getGoodsId());
            map.put("senderId", msg.getSenderId());
            map.put("content", msg.getContent());
            map.put("createTime", msg.getCreateTime());
            map.put("isRead", msg.getIsRead());

            User sender = userMapper.selectById(msg.getSenderId());
            map.put("senderName", sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "系统通知");

            Goods goods = goodsMapper.selectById(msg.getGoodsId());
            map.put("goodsTitle", goods != null ? goods.getTitle() : "已下架物品");

            chatList.add(map);
        }

        // 标记已读
        UpdateWrapper<ChatMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("receiver_id", currentUser.getId()).set("is_read", 1);
        chatMessageMapper.update(null, updateWrapper);

        model.addAttribute("chatList", chatList);
        return "chat/list";
    }
}