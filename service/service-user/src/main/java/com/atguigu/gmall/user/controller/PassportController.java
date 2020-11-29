package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @title: PassportController
 * @Author LiuXianKun
 * @Date: 2020/11/26 20:36
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportController {
    @Resource
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo) {
        if (null == userInfo.getLoginName() || "".equals(userInfo.getLoginName().trim())) {
            return Result.fail().message("用户名不合法");
        }
        if (null == userInfo.getLoginName() || "".equals(userInfo.getLoginName().trim())) {
            return Result.fail().message("密码不合法");
        }

        UserInfo info = userService.login(userInfo);
        if (info != null) {
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            HashMap<String, Object> map = new HashMap<>();
            map.put("nickName", info.getNickName());
            map.put("token", token);
            redisTemplate.opsForValue().set(RedisConst.USER_KEY_PREFIX + token, info.getId().toString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(map);
        }
        return Result.fail().message("用户名或密码错误");
    }


    @GetMapping("logout")
    public Result logout(HttpServletRequest httpServletRequest) {
        redisTemplate.delete(RedisConst.USER_KEY_PREFIX + httpServletRequest.getHeader("token"));
        return Result.ok();
    }

}
