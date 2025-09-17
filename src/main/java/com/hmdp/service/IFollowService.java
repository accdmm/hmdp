package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Accdmm
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    Result follow(long followUserId, boolean isFollow);

    Result isFollow(long followUserId);

    Result followCommons(long followUserId);
}
