package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j  // 添加日志
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取Token
        String token = request.getHeader("Authorization");

        // 调试日志
        log.debug("请求URI: {}, Authorization头: {}", request.getRequestURI(), token);

        if (StrUtil.isBlank(token)) {
            log.warn("Token不存在");
            response.setStatus(401);
            return false;
        }

        try {
            // 2. 处理Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 3. 解析Token
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);

            if (userId == null || username == null) {
                log.warn("Token中缺少用户信息");
                response.setStatus(401);
                return false;
            }

            // 4. 保存用户信息
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setNickName(username);
            UserHolder.saveUser(userDTO);

            log.debug("用户验证成功: {} - {}", userId, username);
            return true;

        } catch (Exception e) {
            log.warn("Token解析失败: {}", token, e);
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal
        UserHolder.removeUser();
    }
}