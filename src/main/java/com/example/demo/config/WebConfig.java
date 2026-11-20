package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🚨 动态获取项目运行所在的盘符/根目录，并在下面建 upload 文件夹
        // 这样不管在哪台电脑，都能完美运行
        String basePath = System.getProperty("user.dir") + "/upload/";

        // 映射物品图片
        registry.addResourceHandler("/upload/goods/**")
                .addResourceLocations("file:" + basePath + "goods/");

        // 映射头像图片
        registry.addResourceHandler("/upload/avatar/**")
                .addResourceLocations("file:" + basePath + "avatar/");
    }
}