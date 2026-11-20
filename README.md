# 校园二手闲置交易平台 - 项目运行说明

## 📁 一、项目说明
本仓库为完整的 Spring Boot 项目源码。
- **GitHub 地址**：https://github.com/yinghuochon/xiaoyuanpingtai

---

## 🛠️ 二、核心技术栈说明
- **核心框架**：Spring Boot 4.0.0, Spring MVC
- **数据持久层**：MyBatis-Plus 3.5.17, MySQL 8.0, HikariCP
- **安全与权限**：Spring Security (BCrypt 强哈希加密, USER/SELLER/ADMIN 三级角色细粒度权限)
- **缓存与并发**：Redis (数据缓存, Hash/List 存储购物车和私信, setnx分布式锁防超卖)
- **消息通知**：JavaMailSender (@Async 异步邮件), RabbitMQ (订单异步解耦与优雅降级)
- **前端技术**：Thymeleaf 服务端渲染, ECharts 数据可视化, 原生 CSS/JS + 动态星空特效
- **部署方案**：Linux + Nginx + Jar 包 + Logback 日志按天切割持久化

---

## 💻 三、环境准备
在运行项目前，请确保你的电脑上已安装并配置好以下环境：
1. **JDK 17**（配置好 `JAVA_HOME` 环境变量）。
2. **MySQL 8.0**。
3. **Redis**（请确保本地已启动 Redis 服务，默认端口 6379）。
4. **IDEA 开发工具**（自带 Maven）。

---

## 🚀 四、启动指南（只需 4 步）

### 【第一步：导入数据库】
打开 Navicat 或其他数据库工具，新建数据库名为 `campus_trade`（字符集选 `utf8mb4`）。导入项目中的 SQL 脚本（如未在仓库中，请向开发者索要），完成建表和测试数据导入。

### 【第二步：启动 Redis 缓存】
确保你本机的 Redis 服务已启动。（如果在 Windows 下，双击 `redis-server.exe`，看到屏幕输出 `"Ready to accept connections"` 即可，项目运行期间别关掉窗口）。

### 【第三步：修改配置文件】
使用 IDEA 打开本项目，找到 `src/main/resources/application.yml` 文件。
找到 `password: 123456`，将其修改为你本机 MySQL 数据库的真实密码。
> 💡 **注**：因演示环境未装 RabbitMQ，配置了 `auto-startup: false` 优雅降级，不影响主流程运行。

### 【第四步：启动项目】
在 IDEA 中等待右下角 Maven 依赖下载完毕。找到 `src/main/java/com/example/demo/DemoApplication.java` 启动类，点击绿色三角形运行。控制台出现 `"Started DemoApplication"` 即启动成功。

---

## 🌐 五、访问地址及测试账号
- **前台访问地址**：http://localhost:8081/goods/
- **后台访问地址**：http://localhost:8081/admin/index

**测试账号（密码均为 `123456`）：**
- 👑 **超级管理员**：`admin` (可进入后台看板、审核物品)
- 🛒 **认证卖家**：`user1` (可发布闲置、接收订单通知)
- 🛍️ **普通买家**：`user2` (可购买、留言、评价)

---

## 📦 附：命令行直接运行 Jar 包指令
在项目根目录（或 `target` 目录）下打开 cmd，执行：
```bash
java -jar demo-0.0.1-SNAPSHOT.jar
