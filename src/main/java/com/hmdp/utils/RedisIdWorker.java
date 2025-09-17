package com.hmdp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIME = 1757203200L;

    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix){
        //设置一下起始时间，时间戳就是起始时间与当前时间的秒数差
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIME;
        //当前日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //生成序列号
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //时间戳左移32位
        return timestamp << COUNT_BITS | count ;
    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(LocalDateTime.now().getYear(), 9, 7, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }
}
