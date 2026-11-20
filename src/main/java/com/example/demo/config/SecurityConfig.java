package com.example.demo.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserMapper userMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 放行公开资源
                        .requestMatchers("/", "/index", "/goods/index", "/goods/detail/**",
                                "/login", "/doLogin", "/register", "/doRegister",
                                "/css/**", "/js/**", "/images/**", "/upload/**",
                                "/user/unreadCount").permitAll()

                        // 🚨 核心修复：发布物品的页面和接口，必须拥有 SELLER 或 ADMIN 角色
                        .requestMatchers("/goods/publish").hasAnyRole("SELLER", "ADMIN")

                        // 订单相关页面需要登录才能访问
                        .requestMatchers("/order/**").authenticated()

                        // 后台管理需要 ADMIN 角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 其他所有请求需要登录
                        .anyRequest().authenticated()
                )
                // 🚨 新增：如果发生 403 权限不足，自动跳回登录页并带上 error 参数
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/login?error=true");
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();

                            QueryWrapper<User> wrapper = new QueryWrapper<>();
                            wrapper.eq("username", username);
                            User user = userMapper.selectOne(wrapper);
                            request.getSession().setAttribute("loginUser", user);

                            java.util.Set<String> roles = org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                            if (roles.contains("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/index");
                            } else {
                                response.sendRedirect("/goods/");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}