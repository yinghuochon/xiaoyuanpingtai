package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // 跳转到登录页面
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            // 🚨 修改这里：让提示语更符合“越权被拦截”的场景
            model.addAttribute("error", "访问被拒绝：您没有权限访问该页面，或账号密码错误！");
        }
        if (logout != null) {
            model.addAttribute("msg", "您已成功退出登录！");
        }
        return "user/login";
    }
}