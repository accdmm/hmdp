package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.JwtUtil;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Accdmm
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(LoginFormDTO loginForm) {
        //校验手机号/邮箱
        String phone = loginForm.getPhone();
        if (!(RegexUtils.isEmailInvalid(phone) || RegexUtils.isPhoneInvalid(phone))) {
            return Result.fail("邮箱或电话格式不正确");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }
        // 根据账号查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = this.getOne(queryWrapper);
        //如果不存在则创建
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        // 保存用户信息到Redis中
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("icon", userDTO.getIcon());
        userMap.put("id", String.valueOf(userDTO.getId()));
        userMap.put("nickName", userDTO.getNickName());
        // 存储redis
        String token = JwtUtil.createToken(user.getId(), user.getNickName());

        stringRedisTemplate.opsForHash().putAll(token, userMap);
        // 设置过期时间（例如60分钟）
        stringRedisTemplate.expire(token, 60, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        //1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2. 获取日期
        LocalDateTime now = LocalDateTime.now();
        //3. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //4. 获取今天是当月第几天(1~31)
        int dayOfMonth = now.getDayOfMonth();
        //5. 写入Redis  BITSET key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok("签到成功~");
    }

    @Override
    public Result signCount() {
        //1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2. 获取日期
        LocalDateTime now = LocalDateTime.now();
        //3. 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //4. 获取今天是当月第几天(1~31)
        int dayOfMonth = now.getDayOfMonth();
        //5. 获取截止至今日的签到记录  BITFIELD key GET uDay 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (result == null || result.isEmpty()) {
            return Result.ok(0);
        }
        //6. 循环遍历
        int count = 0;
        Long num = result.get(0);
        while (true) {
            if ((num & 1) == 0) {
                break;
            } else
                count++;
            //数字右移，抛弃最后一位
            num >>>= 1;
        }
        return Result.ok(count);
    }

    @Override
    public Result getTotalSignCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        int dayOfMonth = now.getDayOfMonth();

        String key = USER_SIGN_KEY + userId + time;
        int total = 0;
            for (int i = 0; i < dayOfMonth; i++) {
                Boolean isSigned = stringRedisTemplate.opsForValue().getBit(key, i);
                if (Boolean.TRUE.equals(isSigned)) {
                    total++;
                }
            }
            return Result.ok(total);
    }


    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(LOGIN_USER_NAME + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
