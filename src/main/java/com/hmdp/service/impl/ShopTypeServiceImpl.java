package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Accdmm
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        // 1. 从 Redis 中获取缓存数据
        String key = CACHE_SHOP_KEY ;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            // 2. 如果缓存存在，直接返回缓存数据
            List<ShopType> typeList = JSONUtil.toList(json, ShopType.class);
            return typeList;
        }
        // 3. 缓存不存在，查询数据库
        List<ShopType> typeList = this.list();


        if (typeList == null || typeList.isEmpty()) {
            return typeList; // 如果数据库中没有数据，直接返回空列表
        }

        // 4. 将查询结果存入 Redis 缓存
        try {
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList), 30L, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("缓存设置失败", e);
        }

        // 5. 返回结果
        return typeList;
    }
}
