package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * @title: UserServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/11/26 20:40
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {

        UserInfo info = userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("login_name", userInfo.getLoginName()
        ).eq("passwd", DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes())));
        if (info != null) {
            return info;
        }

        return null;
    }
}
