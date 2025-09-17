package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.EmailService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Accdmm
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EmailService service;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone) {
        // TODO 发送短信验证码并保存验证码
        // 如果是邮箱格式或电话格式中的一种就通过
        if (!(RegexUtils.isEmailInvalid(phone) || RegexUtils.isPhoneInvalid(phone))) {
            return Result.fail("邮箱或电话格式不正确");
        }

        // 2. 生成6位随机验证码
        // 3. 调用EmailService发送邮件
        try {
            // 4. 将验证码存入 Redis
            String code = service.sendVerificationCode(phone);

            stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 如果发送邮件失败，直接返回错误
            return Result.fail("验证码发送失败，请稍后重试");
        }

        // 5. 返回成功结果
        return Result.ok("验证码发送成功");
    }
    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        // TODO 实现登录功能

        return userService.login(loginForm);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        try {
            log.info("退出登录请求，Authorization头: {}", token);

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.info("要删除的Redis key: {}", token);

            stringRedisTemplate.delete(token);
            
            UserHolder.removeUser();
            return Result.ok("退出成功");
        } catch (Exception e) {
            log.error("退出登录异常: {}", e.getMessage(), e);
            return Result.fail("退出失败");
        }
    }

    @GetMapping("/me")
    public Result me(){
        // TODO 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    // UserController 根据id查询用户
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result singCount(){
        return userService.signCount();
    }

    @GetMapping("/sign/total")
    public Result getTotalSignCount(){
        return userService.getTotalSignCount();
    }
}
