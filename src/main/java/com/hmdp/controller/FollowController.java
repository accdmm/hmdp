package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Accdmm
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") long followUserId, @PathVariable("isFollow") boolean isFollow) {
            return followService.follow(followUserId,isFollow);
    }

    @GetMapping("or/not/{id}")
    public Result isFollow(@PathVariable("id") long followUserId) {
            return followService.isFollow(followUserId);
    }

    @GetMapping("common/{id}")
    public Result followCommons(@PathVariable("id") long followUserId) {
            return followService.followCommons(followUserId);
    }
}
