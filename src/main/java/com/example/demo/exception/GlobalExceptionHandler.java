package com.example.demo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🚨 专门拦截找不到静态资源（如 favicon.ico）的异常，直接忽略，不打印红字
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException e) {
        // 什么都不做，直接吞掉异常，控制台不会再报错
    }

    // 其他真正的系统错误，依然会拦截并跳到 500 页面
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        // 只打印真正的业务异常
        e.printStackTrace();
        model.addAttribute("errorMsg", "系统开小差了，请稍后再试！错误信息：" + e.getMessage());
        return "error/500";
    }
}