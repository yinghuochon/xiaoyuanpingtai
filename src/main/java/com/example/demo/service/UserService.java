package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录验证
     * 注意：这里只是做了最基础的查询比对，
     * 真正的密码加密验证其实是 Spring Security 的 UserDetailsServiceImpl 在做。
     */
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                .eq("password", password);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}