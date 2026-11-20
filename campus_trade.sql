/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : campus_trade

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 18/11/2026 01:30:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '数码产品');
INSERT INTO `category` VALUES (2, '图书教材');
INSERT INTO `category` VALUES (3, '生活用品');
INSERT INTO `category` VALUES (4, '运动户外');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL COMMENT '关联的商品ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '聊天内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 14, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测2】。请尽快前往【我的订单】进行确认交易！', '2026-11-05 22:24:10', 1);
INSERT INTO `chat_message` VALUES (2, 14, 1, 2, '【系统通知】卖家已确认交易完成！您购买的物品【测2】交易成功，快去个人中心给卖家点个好评吧！', '2026-11-05 22:26:08', 1);
INSERT INTO `chat_message` VALUES (3, 15, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试3】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:00:44', 1);
INSERT INTO `chat_message` VALUES (4, 17, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试5】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:05:11', 1);
INSERT INTO `chat_message` VALUES (5, 16, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试4】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:08:04', 1);
INSERT INTO `chat_message` VALUES (6, 16, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试4】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:08:46', 1);
INSERT INTO `chat_message` VALUES (7, 16, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试4】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:09:08', 1);
INSERT INTO `chat_message` VALUES (8, 16, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试4】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:11:27', 1);
INSERT INTO `chat_message` VALUES (9, 16, 2, 1, '【系统通知】您好，买家【user1】刚刚拍下了您的物品【测试4】。请尽快前往【我的订单】进行确认交易！', '2026-11-07 22:12:19', 1);

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `category_id` int NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `price` decimal(10, 2) NOT NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_goods_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 2, 1, '95新 罗技 G502 游戏鼠标', '换了新鼠标，这个闲置了。无拆无修，微动一切正常，送原装配重块和防滑贴。支持宿舍楼下自提。', 129.00, 1, '2026-11-05 10:00:00');
INSERT INTO `goods` VALUES (2, 3, 1, 'iPad Air 4 64G 天空蓝', '吃灰很久了，电池健康度良好，无暗病，屏幕完美。带一个原装保护壳，仅限同城面交验机。', 2100.00, 1, '2026-11-05 10:15:00');
INSERT INTO `goods` VALUES (3, 2, 1, '索尼 WH-1000XM4 降噪耳机', '考研神器，降噪效果无敌。因为换了 AirPods 所以出掉，送收纳包和音频线，成色极新。', 850.00, 1, '2026-11-05 10:30:00');
INSERT INTO `goods` VALUES (4, 3, 2, '考研数学全套资料（张宇/李永乐）', '今年刚上岸，全套资料打包出。包含张宇基础30讲、1000题，李永乐复习全书，笔记很全，希望能帮到学弟学妹。', 50.00, 1, '2026-11-05 11:30:00');
INSERT INTO `goods` VALUES (5, 2, 2, '《C++ Primer Plus》第6版', '专业书，保存完好，内页干净无涂鸦。适合零基础学 C++ 的同学，半价出。', 25.00, 1, '2026-11-05 11:45:00');
INSERT INTO `goods` VALUES (6, 3, 2, '考研英语黄皮书 历年真题解析', '近十年的英语一真题，做了一半，后面没动过。附赠高频词汇本，便宜出给下一届。', 30.00, 1, '2026-11-05 12:00:00');
INSERT INTO `goods` VALUES (7, 3, 3, '小米台灯 1S 护眼版', '毕业清宿舍，九成新，无频闪护眼灯，支持调节亮度和色温。没有包装盒了，功能完好。', 35.00, 1, '2026-11-05 14:20:00');
INSERT INTO `goods` VALUES (8, 2, 3, '南极人 宿舍小功率电吹风', '宿舍用绝对不会跳闸，风力很大，冷热风都有。毕业带不走，便宜给学弟学妹。', 15.00, 1, '2026-11-05 14:30:00');
INSERT INTO `goods` VALUES (9, 3, 3, '多功能宿舍神器 床上书桌', '木质的，很稳当。冬天可以在床上用电脑、看书。毕业了，15块钱速出，需自提。', 15.00, 1, '2026-11-05 14:45:00');
INSERT INTO `goods` VALUES (10, 2, 4, '迪卡侬 山地自行车（带锁）', '骑了一年多，平时很爱惜。刚换的刹车皮和链条，车况很好，送一把 U 型锁。仅限同城面交。', 280.00, 1, '2026-11-05 15:00:00');
INSERT INTO `goods` VALUES (11, 3, 4, '李宁 羽毛球拍双拍套装', '买来打了几次，一直闲置。送三个羽毛球和一桶手胶，适合新手练手，打包带走。', 45.00, 1, '2026-11-05 15:15:00');
INSERT INTO `goods` VALUES (12, 2, 4, '斯伯丁 7号标准室外篮球', '外场打过几次，弹性很好。因为现在都在室内打，把外场这颗出了，物超所值。', 40.00, 1, '2026-11-05 15:30:00');
INSERT INTO `goods` VALUES (14, 1, 1, '测2', '奶蛙是近年在中文互联网上爆红的黄色抽象网络迷因生物：一只长得像青蛙又像人的圆胖小人， 笑起来停不下来。它并非出自某部动画，而是先在短视频与表情包之间长出来、之后才被补上设定的形象。 本词条收录奶蛙的定义、由来与编年史、命名与别称、与奶龙的关系、与真实牛奶蛙的区分、 形象体态、分类谱系、传播机制，以及它在艺术史里的 94 件馆藏。', 6.00, 2, '2026-11-05 22:18:25');
INSERT INTO `goods` VALUES (18, 1, 1, '测试', '奶蛙是近年在中文互联网上爆红的黄色抽象网络迷因生物：一只长得像青蛙又像人的圆胖小人， 笑起来停不下来。它并非出自某部动画，而是先在短视频与表情包之间长出来、之后才被补上设定的形象。 本词条收录奶蛙的定义、由来与编年史、命名与别称、与奶龙的关系、与真实牛奶蛙的区分、 形象体态、分类谱系、传播机制，以及它在艺术史里的 94 件馆藏。', 666.00, 1, '2026-11-08 17:18:32');

-- ----------------------------
-- Table structure for goods_image
-- ----------------------------
DROP TABLE IF EXISTS `goods_image`;
CREATE TABLE `goods_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_img_goods`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_img_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_image
-- ----------------------------
INSERT INTO `goods_image` VALUES (1, 1, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&q=80');
INSERT INTO `goods_image` VALUES (2, 2, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&q=80');
INSERT INTO `goods_image` VALUES (3, 3, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=500&q=80');
INSERT INTO `goods_image` VALUES (4, 4, 'https://images.unsplash.com/photo-1544377193-33dcf4d68fb5?w=500&q=80');
INSERT INTO `goods_image` VALUES (5, 5, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=500&q=80');
INSERT INTO `goods_image` VALUES (6, 6, 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500&q=80');
INSERT INTO `goods_image` VALUES (7, 7, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&q=80');
INSERT INTO `goods_image` VALUES (8, 8, 'https://images.unsplash.com/photo-1522337660859-02fbefca4702?w=500&q=80');
INSERT INTO `goods_image` VALUES (9, 9, 'https://images.unsplash.com/photo-1595515106969-1ce29566ff1c?w=500&q=80');
INSERT INTO `goods_image` VALUES (10, 10, 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=500&q=80');
INSERT INTO `goods_image` VALUES (11, 11, 'https://images.unsplash.com/photo-1613918431703-aa50889e3be9?w=500&q=80');
INSERT INTO `goods_image` VALUES (12, 12, 'https://images.unsplash.com/photo-1519861531473-9200262188bf?w=500&q=80');
INSERT INTO `goods_image` VALUES (14, 14, '/upload/goods/2175dcd7-eb58-4abd-a132-c6b3af72a2d2_OIP-C.png');
INSERT INTO `goods_image` VALUES (18, 18, '/upload/goods/155d87cc-0ba0-489b-a06c-7979263d99a5_1.png');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 12, 2, '物品还在吗？', '2026-11-06 15:15:51', 1);
INSERT INTO `message` VALUES (2, 11, 2, '物品还在吗？', '2026-11-06 15:16:21', 1);
INSERT INTO `message` VALUES (3, 11, 3, '在的', '2026-11-06 15:16:43', 1);
INSERT INTO `message` VALUES (4, 17, 2, '1', '2026-11-06 15:20:27', 1);
INSERT INTO `message` VALUES (5, 17, 1, '2', '2026-11-06 15:20:49', 1);
INSERT INTO `message` VALUES (6, 10, 3, '物品还在吗？', '2026-11-06 15:22:25', 1);
INSERT INTO `message` VALUES (7, 10, 2, '回复 @user2：@user2 在的', '2026-11-06 15:22:40', 1);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `goods_id` bigint NOT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `fk_orders_goods`(`goods_id` ASC) USING BTREE,
  INDEX `fk_orders_buyer`(`buyer_id` ASC) USING BTREE,
  CONSTRAINT `fk_orders_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, '144b1c12a8c749c0ab95e58801e5102f', 14, 2, 1, 6.00, 2, '2026-11-05 22:24:10');
INSERT INTO `orders` VALUES (2, 'MOCK_ORD_01', 1, 2, 3, 50.00, 2, '2026-11-01 22:23:39');
INSERT INTO `orders` VALUES (3, 'MOCK_ORD_02', 2, 2, 3, 129.00, 2, '2026-11-02 22:23:39');
INSERT INTO `orders` VALUES (4, 'MOCK_ORD_03', 3, 2, 3, 25.00, 2, '2026-11-03 22:23:39');
INSERT INTO `orders` VALUES (5, 'MOCK_ORD_04', 4, 2, 3, 280.00, 2, '2026-11-04 22:23:39');
INSERT INTO `orders` VALUES (6, 'MOCK_ORD_05', 5, 2, 3, 850.00, 2, '2026-11-05 22:23:39');
INSERT INTO `orders` VALUES (7, 'MOCK_ORD_06', 6, 2, 3, 45.00, 2, '2026-11-06 22:23:39');
INSERT INTO `orders` VALUES (8, 'MOCK_ORD_07', 7, 2, 3, 15.00, 2, '2026-11-06 22:23:39');

-- ----------------------------
-- Table structure for review
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `goods_id` bigint NOT NULL COMMENT '关联物品ID',
  `reviewer_id` bigint NOT NULL COMMENT '评价人(买家)ID',
  `reviewee_id` bigint NOT NULL COMMENT '被评价人(卖家)ID',
  `rating` int NOT NULL COMMENT '评分 1-5',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review
-- ----------------------------
INSERT INTO `review` VALUES (1, 99, 1, 3, 2, 5, '鼠标很好用，老板人也很爽快，好评！', '2026-11-01 10:00:00');
INSERT INTO `review` VALUES (2, 100, 3, 2, 3, 5, '台灯很新，价格便宜，交易愉快~', '2026-11-02 14:00:00');
INSERT INTO `review` VALUES (3, 99, 1, 3, 2, 5, '鼠标很好用，老板人也很爽快，好评！', '2026-11-01 10:00:00');
INSERT INTO `review` VALUES (4, 100, 7, 2, 3, 5, '台灯很新，价格便宜，交易愉快~', '2026-11-02 14:00:00');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'USER',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `rating_avg` decimal(3, 1) NULL DEFAULT 5.0 COMMENT '平均评分',
  `review_count` int NULL DEFAULT 0 COMMENT '评价数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$C4Sgm3R2LRsSEpWeCJg2cOLl6ufZrqanzf9ukBb8ZYPzDHCDz3ePK', '管理员', NULL, '2335415150@qq.com', 'ADMIN', '2026-11-03 00:55:54', 5.0, 0);
INSERT INTO `sys_user` VALUES (2, 'user1', '$2a$10$C4Sgm3R2LRsSEpWeCJg2cOLl6ufZrqanzf9ukBb8ZYPzDHCDz3ePK', NULL, '/upload/avatar/18c1c1a1-7e2b-42f8-91e4-18c49d0cdcc8_087.png', NULL, 'SELLER', '2026-11-03 01:27:10', 5.0, 1);
INSERT INTO `sys_user` VALUES (3, 'user2', '$2a$10$C4Sgm3R2LRsSEpWeCJg2cOLl6ufZrqanzf9ukBb8ZYPzDHCDz3ePK', 'user2', NULL, '822034169@qq.com', 'SELLER', '2026-11-03 13:52:56', 5.0, 1);
INSERT INTO `sys_user` VALUES (4, 'user3', '$2a$10$Ge0AAd5rtRUj/vZxMNpAZep9XG3mEQiYLKHZrlmDGccgdeAbFJpi2', 'user3', NULL, '', 'USER', '2026-11-06 22:26:38', 5.0, 0);

-- ----------------------------
-- Table structure for wanted
-- ----------------------------
DROP TABLE IF EXISTS `wanted`;
CREATE TABLE `wanted`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发布求购的人',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '求购内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wanted
-- ----------------------------
INSERT INTO `wanted` VALUES (1, 2, '求购：有没有好兄弟出二手的机械键盘？宿舍打游戏用，青轴最好~', '2026-11-05 19:00:00');
INSERT INTO `wanted` VALUES (2, 3, '求购：一本《算法导论》，有笔记也没关系，价格好商量！', '2026-11-05 18:30:00');
INSERT INTO `wanted` VALUES (3, 2, '求购：有没有好兄弟出二手的机械键盘？宿舍打游戏用，青轴最好~', '2026-11-05 19:00:00');
INSERT INTO `wanted` VALUES (4, 3, '求购：一本《算法导论》，有笔记也没关系，价格好商量！', '2026-11-05 18:30:00');
INSERT INTO `wanted` VALUES (5, 2, '求购：收个二手显示器，24寸就行，能亮就行。', '2026-11-05 17:15:00');

SET FOREIGN_KEY_CHECKS = 1;
