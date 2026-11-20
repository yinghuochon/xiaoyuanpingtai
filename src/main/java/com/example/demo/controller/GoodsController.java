package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.config.RabbitMQConfig;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.service.EmailService;
import com.example.demo.service.GoodsService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsImageMapper goodsImageMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WantedMapper wantedMapper;
    @Autowired
    private ReviewMapper reviewMapper;

    // 首页查询
    @GetMapping({"", "/", "/index"})
    public String index(@RequestParam(defaultValue = "1") Integer pageNum,
                        @RequestParam(defaultValue = "8") Integer pageSize, // 🚨 核心改动：每页 8 个
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String minPrice,
                        @RequestParam(required = false) String maxPrice,
                        @RequestParam(required = false, defaultValue = "newest") String sort,
                        @RequestParam(required = false) Integer categoryId,
                        Model model) {

        Page<Goods> resultPage = goodsService.getGoodsPage(pageNum, pageSize, keyword, minPrice, maxPrice, sort, categoryId);

        Map<Long, String> goodsImageMap = new HashMap<>();
        for (Goods goods : resultPage.getRecords()) {
            QueryWrapper<GoodsImage> imgWrapper = new QueryWrapper<>();
            imgWrapper.eq("goods_id", goods.getId()).last("LIMIT 1");
            GoodsImage image = goodsImageMapper.selectOne(imgWrapper);
            if (image != null) {
                goodsImageMap.put(goods.getId(), image.getUrl());
            }

            // 🚨 查卖家信息塞进对象
            User seller = userMapper.selectById(goods.getUserId());
            if (seller != null) {
                goods.setSellerName(seller.getNickname() != null ? seller.getNickname() : seller.getUsername());
                goods.setSellerAvatar(seller.getAvatar());
            }
        }

        QueryWrapper<Wanted> wallWrapper = new QueryWrapper<>();
        wallWrapper.orderByDesc("create_time").last("LIMIT 3");
        List<Wanted> wallWantedList = wantedMapper.selectList(wallWrapper);
        for (Wanted w : wallWantedList) {
            if (w.getContent() != null && w.getContent().length() > 18) {
                w.setContent(w.getContent().substring(0, 18) + "...");
            }
        }

        model.addAttribute("page", resultPage);
        model.addAttribute("goodsImageMap", goodsImageMap);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryList", categoryMapper.selectList(null));
        model.addAttribute("wallWantedList", wallWantedList);

        return "index";
    }

    @GetMapping("/publish")
    public String publishPage(Model model) {
        model.addAttribute("categoryList", categoryMapper.selectList(null));
        return "goods/publish";
    }

    @CacheEvict(value = "goodsList", allEntries = true)
    @PostMapping("/publish")
    public String publish(Goods goods, MultipartFile[] files) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", auth.getName());
        User loginUser = userMapper.selectOne(userWrapper);
        if (loginUser == null) return "redirect:/login";

        goods.setUserId(loginUser.getId());
        goods.setStatus(1);
        goods.setCreateTime(new Date());
        goodsMapper.insert(goods);

        if (files != null && files.length > 0) {
            String uploadDir = "System.getProperty(\"user.dir\") + \"/upload/goods/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(uploadDir + fileName));

                    GoodsImage image = new GoodsImage();
                    image.setGoodsId(goods.getId());
                    image.setUrl("/upload/goods/" + fileName);
                    goodsImageMapper.insert(image);
                }
            }
        }
        return "redirect:/goods/";
    }

    // 物品详情页
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) return "redirect:/goods/";

        try {
            if (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) {
                QueryWrapper<User> userWrapper = new QueryWrapper<>();
                userWrapper.eq("username", authentication.getName());
                User user = userMapper.selectOne(userWrapper);
                if (user != null) {
                    String historyKey = "history:" + user.getId();
                    redisTemplate.opsForList().remove(historyKey, 0, String.valueOf(id));
                    redisTemplate.opsForList().leftPush(historyKey, String.valueOf(id));
                    redisTemplate.opsForList().trim(historyKey, 0, 9);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ 提示：Redis 未连接，跳过浏览足迹记录。");
        }

        QueryWrapper<GoodsImage> imgWrapper = new QueryWrapper<>();
        imgWrapper.eq("goods_id", id);
        List<GoodsImage> images = goodsImageMapper.selectList(imgWrapper);

        QueryWrapper<Message> msgWrapper = new QueryWrapper<>();
        msgWrapper.eq("goods_id", id).orderByAsc("create_time");
        List<Message> messages = messageMapper.selectList(msgWrapper);

        for (Message msg : messages) {
            User msgUser = userMapper.selectById(msg.getUserId());
            msg.setUser(msgUser);
        }

        User seller = userMapper.selectById(goods.getUserId());
        model.addAttribute("seller", seller);

        model.addAttribute("goods", goods);
        model.addAttribute("images", images);
        model.addAttribute("messages", messages);

        return "goods/detail";
    }

    // 获取指定卖家的所有评价（弹窗）
    @GetMapping("/seller/reviews/{sellerId}")
    @ResponseBody
    public List<Review> getSellerReviews(@PathVariable Long sellerId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("reviewee_id", sellerId).orderByDesc("create_time");
        return reviewMapper.selectList(wrapper);
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return "redirect:/goods/";
        }
        model.addAttribute("goods", goods);
        model.addAttribute("categoryList", categoryMapper.selectList(null));
        return "goods/edit";
    }

    @CacheEvict(value = "goodsList", allEntries = true)
    @PostMapping("/edit")
    public String editGoods(Goods goods) {
        goodsMapper.updateById(goods);
        return "redirect:/user/profile";
    }

    @PostMapping("/message/add")
    public String addMessage(Long goodsId, String content, @RequestParam(required = false) String replyTo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username", auth.getName());
        User loginUser = userMapper.selectOne(userWrapper);
        if (loginUser == null) return "redirect:/login";

        if (replyTo != null && !replyTo.isEmpty()) {
            content = "回复 @" + replyTo + "：" + content;
        }

        Message message = new Message();
        message.setGoodsId(goodsId);
        message.setUserId(loginUser.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setIsRead(0);
        messageMapper.insert(message);

        try {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods != null) {
                User seller = userMapper.selectById(goods.getUserId());
                if (seller != null && seller.getEmail() != null && !seller.getEmail().trim().isEmpty()) {
                    emailService.sendMail(
                            seller.getEmail(),
                            "您的物品收到新留言啦！",
                            "尊敬的用户，您发布的物品【" + goods.getTitle() + "】收到了新留言：\n\n" + content + "\n\n请尽快登录校园二手闲置交易平台查看并回复。"
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("发送留言邮件通知失败：" + e.getMessage());
        }

        return "redirect:/goods/detail/" + goodsId;
    }

    @CacheEvict(value = "goodsList", allEntries = true)
    @PostMapping("/delete")
    public String deleteGoods(@RequestParam Long goodsId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            // 🚨 核心修复：把状态改成 0（代表下架），而不是物理删除！
            goods.setStatus(0);
            goodsMapper.updateById(goods);
        }

        redirectAttributes.addFlashAttribute("success", "物品已成功下架！");
        return "redirect:/user/profile";
    }

    @CacheEvict(value = "goodsList", allEntries = true)
    @PostMapping("/buy")
    public String buy(@RequestParam Long goodsId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        String lockKey = "order:lock:" + goodsId + ":" + authentication.getName();
        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isSuccess)) {
            redirectAttributes.addFlashAttribute("error", "请勿重复提交订单！");
            return "redirect:/goods/detail/" + goodsId;
        }

        try {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods == null || goods.getStatus() != 1) {
                redirectAttributes.addFlashAttribute("error", "该物品已售出或不存在！");
                return "redirect:/goods/";
            }

            QueryWrapper<User> buyerWrapper = new QueryWrapper<>();
            buyerWrapper.eq("username", authentication.getName());
            User buyer = userMapper.selectOne(buyerWrapper);

            goods.setStatus(2);
            goodsMapper.updateById(goods);

            Order order = new Order();
            order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
            order.setGoodsId(goods.getId());
            order.setBuyerId(buyer.getId());
            order.setSellerId(goods.getUserId());
            order.setAmount(goods.getPrice());
            order.setStatus(1);
            order.setCreateTime(new Date());
            orderMapper.insert(order);

            // RabbitMQ 异步解耦
            try {
                String msgContent = "用户【" + buyer.getUsername() + "】购买了物品【" + goods.getTitle() + "】，订单号：" + order.getOrderNo();
                rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_ROUTING_KEY, msgContent);
            } catch (Exception e) {
                System.out.println("⚠️ 提示：RabbitMQ 未启动，跳过。");
            }

            // 给卖家发站内信
            try {
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setGoodsId(goods.getId());
                chatMsg.setSenderId(buyer.getId());
                chatMsg.setReceiverId(goods.getUserId());
                chatMsg.setContent("【系统通知】您好，买家【" + buyer.getUsername() + "】刚刚拍下了您的物品【" + goods.getTitle() + "】。请尽快前往【我的订单】进行确认交易！");
                chatMsg.setCreateTime(new Date());
                chatMsg.setIsRead(0);
                chatMessageMapper.insert(chatMsg);
            } catch (Exception e) {
                System.out.println("❌ 发送站内信失败：" + e.getMessage());
            }

            // 给卖家发邮件
            try {
                User seller = userMapper.selectById(goods.getUserId());
                if (seller != null && seller.getEmail() != null && !seller.getEmail().trim().isEmpty()) {
                    String emailContent = "尊敬的用户，您发布的物品【" + goods.getTitle() + "】刚刚被买家【" + buyer.getUsername() + "】拍下啦！\n\n"
                            + "订单号：" + order.getOrderNo() + "\n"
                            + "成交金额：￥" + order.getAmount() + "\n\n"
                            + "请尽快登录平台查看订单，与买家沟通并当面交易。交易完成后请记得在系统中点击【确认交易完成】。";
                    emailService.sendMail(seller.getEmail(), "🔔 您有一笔新订单待确认", emailContent);
                }
            } catch (Exception e) {
                System.out.println("❌ 发送订单邮件失败：" + e.getMessage());
            }

            redirectAttributes.addFlashAttribute("success", "购买成功！已生成订单。");
            return "redirect:/goods/";

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @GetMapping("/testEmail")
    @ResponseBody
    public String testEmail() {
        emailService.sendMail("2335415150@qq.com", "Spring Boot 测试邮件", "这是一封来自你期末大作业的测试邮件！");
        return "邮件发送任务已提交，请查看 IDEA 控制台或 QQ 邮箱！";
    }
}